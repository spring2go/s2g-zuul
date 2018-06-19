package io.spring2go.zuul.mobile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.common.ZuulHeaders;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;
import io.spring2go.zuul.hystrix.ZuulRequestCommandForSemaphoreIsolation;
import io.spring2go.zuul.hystrix.ZuulRequestCommandForThreadIsolation;
import io.spring2go.zuul.util.Debug;
import io.spring2go.zuul.util.HTTPRequestUtil;

public class MobileExecuteRoute extends ZuulFilter {
	private static final DynamicIntProperty maxConnection = DynamicPropertyFactory.getInstance()
			.getIntProperty(Constants.ZUUL_CLIENT_MAX_CONNECTIONS, 500);
	private static final DynamicIntProperty maxRouteConnection = DynamicPropertyFactory.getInstance()
			.getIntProperty(Constants.ZUUL_CLIENT_ROUTE_MAX_CONNECTIONS, 20);

	private static final Runnable loader = new Runnable() {
		@Override
		public void run() {
			MobileExecuteRoute.loadClient();
		}
	};

	private static final AtomicReference<CloseableHttpClient> clientRef = new AtomicReference<CloseableHttpClient>(
			newClient());
	private static final Timer managerTimer = new Timer();

	// cleans expired connections at an interval
	static {
		maxConnection.addCallback(loader);
		maxRouteConnection.addCallback(loader);

		managerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					final CloseableHttpClient hc = clientRef.get();
					if (hc == null)
						return;
					hc.getConnectionManager().closeExpiredConnections();
				} catch (Throwable t) {
					Cat.logError("error closing expired connections", t);
				}
			}
		}, 30000, 5000);
	}

	private static final HttpClientConnectionManager newConnectionManager() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(maxConnection.get());
		cm.setDefaultMaxPerRoute(maxRouteConnection.get());
		return cm;
	}

	public static final void loadClient() {
		final CloseableHttpClient oldClient = clientRef.get();
		clientRef.set(newClient());
		if (oldClient != null) {
			managerTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						oldClient.close();
					} catch (Throwable t) {
						Cat.logError("error shutting down old connection manager", t);
					}
				}
			}, 30000);
		}

	}

	private static final CloseableHttpClient newClient() {
		// I could statically cache the connection manager but we will probably
		// want to make some of its properties
		// dynamic in the near future also
		RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
		RedirectStrategy redirectStrategy = new RedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
				return false;
			}

			@Override
			public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse,
					HttpContext httpContext) {
				return null;
			}
		};
		CloseableHttpClient httpclient = HttpClients.custom().disableContentCompression()
				.setConnectionManager(newConnectionManager()).setDefaultRequestConfig(config)
				.setRetryHandler(retryHandler).setRedirectStrategy(redirectStrategy).disableCookieManagement().build();
		return httpclient;
	}

	@Override
	public String filterType() {
		return "route";
	}

	public int filterOrder() {
		return 20;

	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRouteUrl() != null && ctx.sendZuulResponse();
	}

	@Override
	public Object run() throws ZuulException {
		Transaction tran = Cat.getProducer().newTransaction("Filter", "ExecuteRoute");
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String url = ctx.getRouteUrl().toString();
		String groupName = null;
		String routeName = null;

		try {
			Cat.logEvent("route.url", url);
			HttpClient httpclient = clientRef.get();
			Collection<Header> headers = buildZuulRequestHeaders(request);
			InputStream requestEntity = getRequestBody(request);
			int contentLength = request.getContentLength();
			groupName = ctx.getRouteGroup();
			routeName = ctx.getRouteName();

			if (groupName == null)
				groupName = Constants.DEFAULT_GROUP;
			if (routeName == null)
				routeName = Constants.DEFAULT_NAME;
			RequestConfig requestConfig = buildRequestConfig(routeName, groupName);
			String verb = request.getMethod().toUpperCase();

			HttpResponse response = forward(httpclient, requestConfig, verb, url, headers, requestEntity, contentLength,
					groupName, routeName);
			setResponse(response);
			tran.setStatus(Transaction.SUCCESS);

		} catch (Exception e) {
			tran.setStatus(e);
			String originUrl = getOriginatingURL();
			String targetUrl = url;
			String targetIp = "unknown";
			try {
				targetIp = InetAddress.getByName(ctx.getRouteUrl().getHost()).getHostAddress();
			} catch (Exception ignore) { }

			Exception ex = e;
			//String errorMsg =String.format("[${ex.class.simpleName}]{${ex.message}}   ");
			String errorMsg =String.format("[%s]{%s}   ",ex.getClass().getSimpleName(),ex.getMessage());
			Throwable cause = null;
			while ((cause = ex.getCause()) != null) {
				ex = (Exception) cause;
				//errorMsg = "${errorMsg}[${ex.class.simpleName}]{${ex.message}}   ";
				errorMsg += String.format("[%s]{%s}   ",ex.getClass().getSimpleName(),ex.getMessage());
			}

			//Cat.logError("Service Execution Error,OriginUrl: ${originUrl}\nTargetUrl: ${targetUrl}\nTargetIp: ${targetIp}\nCause: ${errorMsg}", e);
			Cat.logError(String.format("Service Execution Error,OriginUrl: %s\nTargetUrl: %s\nTargetIp: %s\nCause: %s\n,groupName:%s\n,routeName:%s",originUrl,targetUrl,targetIp,errorMsg,groupName,routeName), e);
			//throw new ZuulException(errorMsg,500, "TargetUrl: ${targetUrl}\nCause: ${errorMsg}");
			throw new ZuulException(errorMsg,500, String.format("TargetUrl: %s\nCause: %s",targetUrl,errorMsg));
		} finally {
			tran.complete();
		}

		return null;
	}

	private String getOriginatingURL() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();

		String protocol = request.getHeader("x-forwarded-proto");
		if (protocol == null)
			protocol = "http";
		if (request.getLocalPort() == 8443)
			protocol = "https";
		String host = request.getHeader("host");
		String uri = request.getRequestURI();
		//String URL = "${protocol}://${host}${uri}";
		String URL = String.format("%s://%s%s", protocol,host,uri);
		if (request.getQueryString() != null) {
			//URL += "?${request.getQueryString()}";
			URL+="?"+request.getQueryString();
		}
		return URL;
	}

	public void setResponse(HttpResponse response) throws Exception, IOException {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.setResponseStatusCode(response.getStatusLine().getStatusCode());
		boolean isOriginResponseGZipped = false;
		for (Header header : response.getAllHeaders()) {
			if (header.getName().equalsIgnoreCase(ZuulHeaders.CONTENT_LENGTH)) {
				ctx.setOriginContentLength(header.getValue());
			}
			if (isValidZuulResponseHeader(header.getName())) {
				 ctx.addZuulResponseHeader(header.getName(), header.getValue());
			}
			if (header.getName().equalsIgnoreCase(ZuulHeaders.CONTENT_ENCODING)) {
				if (HTTPRequestUtil.isGzipped(header.getValue())) {
					isOriginResponseGZipped = true;
				}
			}
			if (Debug.debugRequest()) {
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < ${header.name}, ${header.value}");
			}
		}

		ctx.setResponseGZipped(isOriginResponseGZipped);
		InputStream inputStream = response.getEntity().getContent();
		if (Debug.debugRequest()) {
			if (inputStream == null) {
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < null ");
			}else{
				byte[] origBytes = getBytes(inputStream,8096);
				byte[] contentBytes = origBytes;
				if (isOriginResponseGZipped) {
					contentBytes = getBytes(new GZIPInputStream(new ByteArrayInputStream(contentBytes)),8096);
				}
				String entity = new String(contentBytes);
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < ${entity}");			
				inputStream = new ByteArrayInputStream(origBytes);
			}
		}
		ctx.setResponseDataStream(inputStream);
	}

	public Collection<Header> buildZuulRequestHeaders(HttpServletRequest request) {
		Map<String, Header> headersMap = new HashMap<String, Header>();

		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = ((String) headerNames.nextElement()).toLowerCase();
			String value = request.getHeader(name);
			if (isValidZuulRequestHeader(name)) {
				headersMap.put(name, new BasicHeader(name, value));
			}
		}

		Map<String, String> zuulRequestHeaders = RequestContext.getCurrentContext().getZuulRequestHeaders();

		for (Entry<String, String> entry : zuulRequestHeaders.entrySet()) {
			headersMap.put(entry.getKey(), new BasicHeader(entry.getKey(), entry.getValue()));
		}

		if (RequestContext.getCurrentContext().getResponseGZipped()) {
			String name = "accept-encoding";
			String value = "gzip";
			headersMap.put(name, new BasicHeader(name, value));
		}

		return headersMap.values();
	}

	public HttpResponse forward(HttpClient httpclient, RequestConfig requestConfig, String verb, String url,
			Collection<Header> headers, InputStream requestEntity, int contentLength, String groupName,
			String routeName) throws IOException {
		requestEntity = debug(verb, url, headers, requestEntity, contentLength);
		HttpUriRequest httpUriRequest;

		switch (verb) {
		case "POST":
			httpUriRequest = new HttpPost(url);
			((HttpPost) httpUriRequest).setConfig(requestConfig);
			InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength);
			((HttpPost) httpUriRequest).setEntity(entity);
			break;
		case "PUT":
			httpUriRequest = new HttpPut(url);
			((HttpPut) httpUriRequest).setConfig(requestConfig);
			InputStreamEntity entity1 = new InputStreamEntity(requestEntity, contentLength);
			((HttpPost) httpUriRequest).setEntity(entity1);
			break;
		default:			
			httpUriRequest = RequestBuilder.create(verb).setUri(url).setConfig(requestConfig).build();

		}

		for (Header header : headers) {
			httpUriRequest.addHeader(header);
		}

		String isolationStrategy = DynamicPropertyFactory.getInstance().getStringProperty(routeName + ".isolation.strategy", null).get();
		if (isolationStrategy == null) {
			isolationStrategy = DynamicPropertyFactory.getInstance().getStringProperty(groupName + ".isolation.strategy", null).get();
		}
		if (isolationStrategy == null) {
			isolationStrategy = DynamicPropertyFactory.getInstance().getStringProperty("zuul.isolation.strategy.global", "SEMAPHORE").get();
		}

		long start = System.currentTimeMillis();
		try {
			if ("THREAD".equalsIgnoreCase(isolationStrategy)) {
				return new ZuulRequestCommandForThreadIsolation(httpclient, httpUriRequest, groupName, routeName).execute();
			} else {
				return new ZuulRequestCommandForSemaphoreIsolation(httpclient, httpUriRequest, groupName, routeName).execute();
			}
		} finally {
			RequestContext.getCurrentContext().set("remoteCallCost", System.currentTimeMillis() - start);
		}
	}

	public InputStream debug(String verb, String url, Collection<Header> headers, InputStream requestEntity,
			int contentLength) throws IOException {
		if (Debug.debugRequest()) {
			RequestContext.getCurrentContext().addZuulResponseHeader("x-target-url", url);
			Debug.addRequestDebug("ZUUL:: url=${url}");
			for (Header it : headers) {
				Debug.addRequestDebug("ZUUL::> ${it.name}  ${it.value}");
			}
			if (requestEntity != null) {
				requestEntity = debugRequestEntity(requestEntity,contentLength);
			}
		}
		return requestEntity;
	}

	private InputStream debugRequestEntity(InputStream inputStream,int contentLength) throws IOException {
		if (Debug.debugRequestHeadersOnly())
			return inputStream;
		if (inputStream == null)
			return null;
		byte[] entityBytes = getBytes(inputStream,contentLength);
		String entity = new String(entityBytes);
		Debug.addRequestDebug("ZUUL::> ${entity}");
		return new ByteArrayInputStream(entityBytes);
	}

	private byte[] getBytes(InputStream is,int contentLength) throws IOException {
		ByteArrayOutputStream answer = new ByteArrayOutputStream();
		// reading the content of the file within a byte buffer
		byte[] byteBuffer = new byte[contentLength];
		int nbByteRead /* = 0 */;
		try {
			while ((nbByteRead = is.read(byteBuffer)) != -1) {
				// appends buffer
				answer.write(byteBuffer, 0, nbByteRead);
			}
		} finally {
			//closeWithWarning(is);
		}
		return answer.toByteArray();
	}	

	public boolean isValidZuulRequestHeader(String name) {
		if (name.toLowerCase().contains("content-length"))
			return false;
		if (!RequestContext.getCurrentContext().getResponseGZipped()) {
			if (name.toLowerCase().contains("accept-encoding"))
				return false;
		}
		if(name.toLowerCase().equalsIgnoreCase("host")){
			return false;
		}
		return true;
	}

	public boolean isValidZuulResponseHeader(String name) {
		switch (name.toLowerCase()) {
		case "connection":
		case "content-length":
		case "content-encoding":
		case "server":
		case "host":
		case "transfer-encoding":
		case "access-control-allow-origin":
		case "access-control-allow-headers":
			return false;
		default:
			return true;
		}
	}

	public InputStream getRequestBody(HttpServletRequest request) {
		InputStream requestEntity = null;
		try {
			requestEntity = request.getInputStream();
		} catch (IOException e) {
			// no requestBody is ok.
		}
		return requestEntity;
	}

	private RequestConfig buildRequestConfig(String routName, String groupName) {

		RequestConfig.Builder builder = RequestConfig.custom();

		int connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty(routName + ".connect.timeout", 0)
				.get();
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty(groupName + ".connect.timeout", 0)
					.get();
		}
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("zuul.connect.timeout.global", 2000)
					.get();
		}
		builder.setConnectTimeout(connectTimeout);

		int socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty(routName + ".socket.timeout", 0).get();
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty(groupName + ".socket.timeout", 0).get();
		}
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty("zuul.socket.timeout.global", 1000)
					.get();
		}
		builder.setSocketTimeout(socketTimeout);

		int requestConnectionTimeout = DynamicPropertyFactory.getInstance()
				.getIntProperty(routName + ".request.connection.timeout", 0).get();
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.getInstance()
					.getIntProperty(groupName + ".request.connection.timeout", 0).get();
		}
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.getInstance()
					.getIntProperty("zuul.request.connection.timeout.global", 10).get();
		}
		builder.setConnectionRequestTimeout(requestConnectionTimeout);

		return builder.build();
	}
}

