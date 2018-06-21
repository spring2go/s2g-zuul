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

	private static final AtomicReference<HashMap<String, URL>> routingTableRef = new AtomicReference<HashMap<String, URL>>();

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
		HashMap<String, URL> routingTable = new HashMap<String, URL>();
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
				try {
					URL url = new URL(urlString);
					routingTable.put(svcName, url);
				} catch (MalformedURLException e) {
					logger.error("malformed url : " + urlString);
				}
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
		return true;
	}

	
	// sample url
	// http://api.spring2go.com/api/hello
	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		String path = ctx.getRequest().getRequestURI();
		String serviceName = "";
		int index = path.lastIndexOf("/api/");
		if (index > 0) {
			serviceName = path.substring(index + 5);
			if (StringUtils.isNotEmpty(serviceName)) {
				if (serviceName.indexOf("/") > 0) {
					serviceName = serviceName.split("/")[0];
				}
			}
		}
		if (StringUtils.isNotEmpty(serviceName)) {
			URL url = routingTableRef.get().get(serviceName);
			if (url != null) {
				ctx.setRouteUrl(url);
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
