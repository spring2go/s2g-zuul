package io.spring2go.zuul.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.common.HttpServletRequestWrapper;
import io.spring2go.zuul.common.HttpServletResponseWrapper;
import io.spring2go.zuul.context.RequestContext;

/**
 * This class initializes servlet requests and responses into the RequestContext
 * and wraps the FilterProcessor calls to preRoute(), route(), postRoute(), and
 * error() methods
 *
 */
public class ZuulRunner {

	/**
	 * Creates a new <code>ZuulRunner</code> instance.
	 */
	public ZuulRunner() {
	}

	/**
	 * sets HttpServlet request and HttpResponse
	 *
	 * @param servletRequest
	 * @param servletResponse
	 */
	public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		RequestContext.getCurrentContext().setRequest(new HttpServletRequestWrapper(servletRequest));
		RequestContext.getCurrentContext().setResponse(new HttpServletResponseWrapper(servletResponse));
	}

	/**
	 * executes "pre" filterType ZuulFilters
	 *
	 * @throws ZuulException
	 */
	public void preRoute() throws ZuulException {
		FilterProcessor.getInstance().preRoute();
	}

	/**
	 * executes "route" filterType ZuulFilters
	 *
	 * @throws ZuulException
	 */
	public void route() throws ZuulException {
		FilterProcessor.getInstance().route();
	}

	/**
	 * executes "post" filterType ZuulFilters
	 *
	 * @throws ZuulException
	 */
	public void postRoute() throws ZuulException {
		FilterProcessor.getInstance().postRoute();
	}

	/**
	 * executes "error" filterType ZuulFilters
	 * @throws ZuulException 
	 */
	public void error() throws ZuulException {
		FilterProcessor.getInstance().error();
	}

}
