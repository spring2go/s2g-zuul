package io.spring2go.zuul.mobile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;

public class TestRouting extends ZuulFilter {

	private static Logger logger = LoggerFactory.getLogger(TestRouting.class);

	private static final AtomicReference<HashMap<String, String>> routingTableRef = new AtomicReference<HashMap<String, String>>();

	private static final DynamicStringProperty ROUTING_TABLE_STRING_PROPERTY = DynamicPropertyFactory.getInstance()
			.getStringProperty("zuul.routing_table_string", null);

	static {
		buildRoutingTable();

		ROUTING_TABLE_STRING_PROPERTY.addCallback(new Runnable() {

			@Override
			public void run() {
				buildRoutingTable();
			}

		});
	}

	static void buildRoutingTable() {
		logger.info("building routing table");
		HashMap<String, String> routingTable = new HashMap<String, String>();
		String routingTableString = ROUTING_TABLE_STRING_PROPERTY.get();
		if (StringUtils.isEmpty(routingTableString)) {
			logger.warn("routing table string is empty, nothing to build");
			return;
		}
		String[] routingEntries = routingTableString.split("&");
		for (String routingEntry : routingEntries) {
			String[] kvs = routingEntry.split("@");
			if (kvs.length == 2) {
				String svcName = kvs[0];
				String urlString = kvs[1];
				routingTable.put(svcName, urlString);
				logger.info("added route entry key = " + svcName + ", value = " + urlString);
			}
		}
		if (routingTable.size() > 0) {
			routingTableRef.set(routingTable);
			logger.info("routing table updated, entry size " + routingTable.size());
		} else {
			logger.info("routing table is not updated, entry size is 0");
		}
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.sendZuulResponse();
	}

	// sample url
	// http://api.spring2go.com/api/hello
	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		String path = ctx.getRequest().getRequestURI();
		String requestPath = "";
		String serviceName = "";
		String requestMethod = "";
		int index = path.lastIndexOf("/api/");
		if (index > 0) {
			requestPath = path.substring(index + 5);
			if (StringUtils.isNotEmpty(requestPath)) {
				int index2 = requestPath.indexOf("/");
				if (index2 > 0) {
					serviceName = requestPath.substring(0, index2);
					requestMethod = requestPath.substring(index2 + 1);
				} else {
					serviceName = requestPath;
				}
			}
		}
		if (StringUtils.isNotEmpty(serviceName)) {
			String urlString = routingTableRef.get().get(serviceName);
			if (StringUtils.isNotEmpty(urlString)) {
				if (StringUtils.isNotEmpty(requestMethod)) {
					urlString = urlString.endsWith("/") ? urlString + requestMethod : urlString + "/" + requestMethod;
				}
				URL url;
				try {
					url = new URL(urlString);
					ctx.setRouteUrl(url);
				} catch (MalformedURLException e) {
					throw new ZuulException(e, "Malformed URL exception", 500, "Malformed URL exception");
				}
			}
		}

		if (ctx.getRouteUrl() == null) {
			throw new ZuulException("No route found", 404, "No route found");
		}

		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 20;
	}

}
