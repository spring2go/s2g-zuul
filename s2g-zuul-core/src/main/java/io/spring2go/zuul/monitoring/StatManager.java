package io.spring2go.zuul.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring2go.zuul.context.RequestContext;

public class StatManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatManager.class);

	protected static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]+");
	// should match *.ctrip.com, or raw IP addresses.
	protected static final Pattern HOST_PATTERN = Pattern
			.compile("(?:(.+)\\.spring2go\\.com)|((?:\\d{1,3}\\.?){4})|(ip-\\d+-\\d+-\\d+-\\d+)");

	protected static final String HOST_HEADER = "host";
	protected static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";
	protected static final String X_FORWARDED_PROTO_HEADER = "x-forwarded-proto";

	protected final ConcurrentMap<String, ConcurrentHashMap<Integer, RouteStatusCodeMonitor>> routeStatusMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, RouteStatusCodeMonitor>>();
	protected final ConcurrentMap<String, NamedCountingMonitor> namedStatusMap = new ConcurrentHashMap<String, NamedCountingMonitor>();
	protected final ConcurrentMap<String, NamedCountingMonitor> hostCounterMap = new ConcurrentHashMap<String, NamedCountingMonitor>();
	protected final ConcurrentMap<String, NamedCountingMonitor> protocolCounterMap = new ConcurrentHashMap<String, NamedCountingMonitor>();
	protected final ConcurrentMap<String, NamedCountingMonitor> ipVersionCounterMap = new ConcurrentHashMap<String, NamedCountingMonitor>();

	protected final ConcurrentHashMap<String, ConcurrentHashMap<String, RouteErrorMonitor>> routeErrorMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, RouteErrorMonitor>>();

	protected static StatManager instance = new StatManager();

	public static StatManager getManager() {
		return instance;
	}

	/**
	 * Collects counts statistics about the request: client ip address from the
	 * x-forwarded-for header; ipv4 or ipv6 and host name from the host header;
	 *
	 * @param req
	 */
	public void collectRequestStats(HttpServletRequest req) {
		// ipv4/ipv6 tracking
		String clientIp;
		final String xForwardedFor = req.getHeader(X_FORWARDED_FOR_HEADER);
		if (xForwardedFor == null) {
			clientIp = req.getRemoteAddr();
		} else {
			clientIp = extractClientIpFromXForwardedFor(xForwardedFor);
		}

		final boolean isIPv6 = (clientIp != null) ? isIPv6(clientIp) : false;
		final String ipVersionKey = isIPv6 ? "ipv6" : "ipv4";
		incrementNamedCountingMonitor(ipVersionKey, ipVersionCounterMap);

		// host header
		String host = req.getHeader(HOST_HEADER);
		if (host != null) {
			int colonIdx;
			if (isIPv6) {
				// an ipv6 host might be a raw IP with 7+ colons
				colonIdx = host.lastIndexOf(":");
			} else {
				// strips port from host
				colonIdx = host.indexOf(":");
			}
			if (colonIdx > -1)
				host = host.substring(0, colonIdx);
			incrementNamedCountingMonitor(hostKey(host), this.hostCounterMap);
		}

		// http vs. https
		String protocol = req.getHeader(X_FORWARDED_PROTO_HEADER);
		if (protocol == null)
			protocol = req.getScheme();
		incrementNamedCountingMonitor(protocolKey(protocol), this.protocolCounterMap);
	}

	/**
	 * collects and increments counts of status code, route/status code and
	 * status_code bucket, eg 2xx 3xx 4xx 5xx
	 *
	 * @param route
	 * @param statusCode
	 */
	public void collectRouteStatusStats(String route, int statusCode) {

		// increments 200, 301, 401, 503, etc. status counters
		final String preciseStatusString = String.format("status_%d", statusCode);
		NamedCountingMonitor preciseStatus = namedStatusMap.get(preciseStatusString);
		if (preciseStatus == null) {
			preciseStatus = new NamedCountingMonitor(preciseStatusString);
			NamedCountingMonitor found = namedStatusMap.putIfAbsent(preciseStatusString, preciseStatus);
			if (found != null)
				preciseStatus = found;
			else
				MonitorRegistry.getInstance().registerObject(preciseStatus);
		}
		preciseStatus.increment();

		// increments 2xx, 3xx, 4xx, 5xx status counters
		final String summaryStatusString = String.format("status_%dxx", statusCode / 100);
		NamedCountingMonitor summaryStatus = namedStatusMap.get(summaryStatusString);
		if (summaryStatus == null) {
			summaryStatus = new NamedCountingMonitor(summaryStatusString);
			NamedCountingMonitor found = namedStatusMap.putIfAbsent(summaryStatusString, summaryStatus);
			if (found != null)
				summaryStatus = found;
			else
				MonitorRegistry.getInstance().registerObject(summaryStatus);
		}
		summaryStatus.increment();

		// increments route and status counter
		if (route == null)
			route = "ROUTE_NOT_FOUND";
		route = route.replace("/", "_");
		ConcurrentHashMap<Integer, RouteStatusCodeMonitor> statsMap = routeStatusMap.get(route);
		if (statsMap == null) {
			statsMap = new ConcurrentHashMap<Integer, RouteStatusCodeMonitor>();
			ConcurrentHashMap<Integer, RouteStatusCodeMonitor> conflict = routeStatusMap.putIfAbsent(route, statsMap);
			if (conflict != null)
				statsMap = conflict;
		}
		RouteStatusCodeMonitor sd = statsMap.get(statusCode);
		if (sd == null) {
			// don't register only 404 status codes (these are garbage
			// endpoints)
			if (statusCode == 404) {
				if (statsMap.size() == 0) {
					return;
				}
			}

			sd = new RouteStatusCodeMonitor(route, statusCode);
			RouteStatusCodeMonitor sd1 = statsMap.putIfAbsent(statusCode, sd);
			if (sd1 != null) {
				sd = sd1;
			} else {
				MonitorRegistry.getInstance().registerObject(sd);
			}
		}
		sd.update();
	}
	
    /**
     * updates count for the given route and error cause
     * @param route
     * @param cause
     */
    public void collectRouteErrorStats(String route, String cause) {
        if (route == null) route = RequestContext.getCurrentContext().getRequest().getRequestURI();
        if (route == null) route = "UNKNOWN_ROUTE";
        route = route.replace("/", "_");
        ConcurrentHashMap<String, RouteErrorMonitor> statsMap = routeErrorMap.get(route);
        if (statsMap == null) {
            statsMap = new ConcurrentHashMap<String, RouteErrorMonitor>();
            routeErrorMap.putIfAbsent(route, statsMap);
            statsMap = routeErrorMap.get(route);
        }
        RouteErrorMonitor sd = statsMap.get(cause);
        if (sd == null) {
            sd = new RouteErrorMonitor(route, cause);
            RouteErrorMonitor sd1 = statsMap.putIfAbsent(cause, sd);
            if (sd1 != null) {
                sd = sd1;
            } else {
                MonitorRegistry.getInstance().registerObject(sd);
            }
        }
        sd.update();
    }

	protected NamedCountingMonitor getHostMonitor(String host) {
		return this.hostCounterMap.get(hostKey(host));
	}

	protected NamedCountingMonitor getProtocolMonitor(String proto) {
		return this.protocolCounterMap.get(protocolKey(proto));
	}

	private static final String hostKey(String host) {
		try {
			final Matcher m = HOST_PATTERN.matcher(host);

			// I know which type of host matched by the number of the group that
			// is non-null
			// I use a different replacement string per host type to make the
			// Epic stats more clear
			if (m.matches()) {
				if (m.group(1) != null)
					host = host.replace(m.group(1), "SPRING2GO");
				else if (m.group(2) != null)
					host = host.replace(m.group(2), "IP");
				else if (m.group(3) != null)
					host = host.replace(m.group(3), "IP");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			return String.format("host_%s", host);
		}
	}

	protected static final String protocolKey(String proto) {
		return String.format("protocol_%s", proto);
	}

	protected static final boolean isIPv6(String ip) {
		return ip.split(":").length == 8;
	}

	protected static final String extractClientIpFromXForwardedFor(String xForwardedFor) {
		return xForwardedFor.split(",")[0];
	}

	/**
	 * helper method to create new monitor, place into map, and register wtih
	 * Epic, if necessary
	 */
	protected void incrementNamedCountingMonitor(String name, ConcurrentMap<String, NamedCountingMonitor> map) {
		NamedCountingMonitor monitor = map.get(name);
		if (monitor == null) {
			monitor = new NamedCountingMonitor(name);
			NamedCountingMonitor conflict = map.putIfAbsent(name, monitor);
			if (conflict != null)
				monitor = conflict;
			else
				MonitorRegistry.getInstance().registerObject(monitor);
		}
		monitor.increment();
	}

	/**
	 * For Test
	 * 
	 * @param route
	 * @param statusCode
	 * @return the RouteStatusCodeMonitor for the given route and status code
	 */
	public RouteStatusCodeMonitor getRouteStatusCodeMonitor(String route, int statusCode) {
		Map<Integer, RouteStatusCodeMonitor> map = routeStatusMap.get(route);
		if (map == null)
			return null;
		return map.get(statusCode);
	}
	
    /**
    *
    * @param route
    * @param cause
    * @return data structure for holding count information for a route and cause
    */
   public RouteErrorMonitor getStats(String route, String cause) {
       Map<String, RouteErrorMonitor> map = routeErrorMap.get(route);
       if (map == null) return null;
       return map.get(cause);
   }
}
