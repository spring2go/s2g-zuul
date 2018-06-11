package io.spring2go.zuul.util;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import io.spring2go.zuul.context.RequestContext;

/**
 * Some handy methods for workign with HTTP requests
 * 
 */
public class HTTPRequestUtil {


	public static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";

	/**
	 * Get the IP address of client making the request.
	 *
	 * Uses the "x-forwarded-for" HTTP header if available, otherwise uses the
	 * remote IP of requester.
	 *
	 * @param request
	 *            <code>HttpServletRequest</code>
	 * @return <code>String</code> IP address
	 */
	public static String getClientIP(HttpServletRequest request) {
		final String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
		String clientIP = null;
		if (xForwardedFor == null) {
			clientIP = request.getRemoteAddr();
		} else {
			clientIP = extractClientIpFromXForwardedFor(xForwardedFor);
		}
		return clientIP;
	}

	/**
	 * Extract the client IP address from an x-forwarded-for header. Returns
	 * null if there is no x-forwarded-for header
	 *
	 * @param xForwardedFor
	 *            a <code>String</code> value
	 * @return a <code>String</code> value
	 */
	public static String extractClientIpFromXForwardedFor(String xForwardedFor) {
		if (xForwardedFor == null) {
			return null;
		}
		xForwardedFor = xForwardedFor.trim();
		String tokenized[] = xForwardedFor.split(",");
		if (tokenized.length == 0) {
			return null;
		} else {
			return tokenized[0].trim();
		}
	}


	/**
	 * returns the Header value for the given sHeaderName
	 *
	 * @param sHeaderName
	 *            a <code>String</code> value
	 * @return a <code>String</code> value
	 */
	public static String getHeaderValue(String sHeaderName) {
		return RequestContext.getCurrentContext().getRequest().getHeader(sHeaderName);
	}

	/**
	 * returns a form value from a given sHeaderName
	 *
	 * @param sHeaderName
	 *            a <code>String</code> value
	 * @return a <code>String</code> value
	 */
	public static String getFormValue(String sHeaderName) {
		return RequestContext.getCurrentContext().getRequest().getParameter(sHeaderName);
	}

	/**
	 * returns query params as a Map with String keys and Lists of Strings as
	 * values
	 * 
	 * @return
	 */
	public static Map<String, List<String>> getQueryParams() {

		Map<String, List<String>> qp = RequestContext.getCurrentContext().getRequestQueryParams();
		if (qp != null)
			return qp;

		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();

		qp = new HashMap<String, List<String>>();

		if (request.getQueryString() == null)
			return null;
		StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
		int i;

		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			i = s.indexOf("=");
			if (i > 0 && s.length() > i + 1) {
				String name = s.substring(0, i);
				String value = s.substring(i + 1);

				try {
					name = URLDecoder.decode(name, "UTF-8");
				} catch (Exception e) {
				}
				try {
					value = URLDecoder.decode(value, "UTF-8");
				} catch (Exception e) {
				}

				List valueList = qp.get(name);
				if (valueList == null) {
					valueList = new LinkedList<String>();
					qp.put(name, valueList);
				}

				valueList.add(value);
			}
		}

		RequestContext.getCurrentContext().setRequestQueryParams(qp);
		return qp;
	}

	/**
	 * Checks headers, query string, and form body for a given parameter
	 *
	 * @param sName
	 * @return
	 */
	public static String getValueFromRequestElements(String sName) {
		String sValue = null;
		if (getQueryParams() != null) {
			final List<String> v = getQueryParams().get(sName);
			if (v != null && !v.isEmpty())
				sValue = v.iterator().next();
		}
		if (sValue != null)
			return sValue;
		sValue = getHeaderValue(sName);
		if (sValue != null)
			return sValue;
		sValue = getFormValue(sName);
		if (sValue != null)
			return sValue;
		return null;
	}

	/**
	 * return true if the client requested gzip content
	 *
	 * @param contentEncoding
	 *            a <code>String</code> value
	 * @return true if the content-encoding param containg gzip
	 */
	public static boolean isGzipped(String contentEncoding) {
		return contentEncoding.contains("gzip");
	}

}
