package io.spring2go.zuul.core;

import java.net.URL;
import java.util.concurrent.Callable;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.context.RequestContext;

public class ZuulCallable implements Callable {

	private static Logger LOGGER = LoggerFactory.getLogger(ZuulCallable.class);

	private AsyncContext ctx;
	private ZuulRunner zuulRunner;
	private Context catCtx;
	private HttpServletRequest request;

	public ZuulCallable(Context catContext, AsyncContext asyncContext, ZuulRunner zuulRunner,
			HttpServletRequest request) {
		this.ctx = asyncContext;
		this.zuulRunner = zuulRunner;
		this.catCtx = catContext;
		this.request = request;
	}

	@Override
	public Object call() throws Exception {
		Cat.logRemoteCallServer(catCtx);
		RequestContext.getCurrentContext().unset();
		Transaction tran = ((DefaultMessageProducer) Cat.getProducer()).newTransaction("ZuulCallable",
				request.getRequestURL().toString());
		RequestContext zuulContext = RequestContext.getCurrentContext();
		long start = System.currentTimeMillis();
		try {
			tran.setStatus(Transaction.SUCCESS);
			service(ctx.getRequest(), ctx.getResponse());
		} catch (Throwable t) {
			LOGGER.error("ZuulCallable execute error.", t);
			Cat.logError(t);
			tran.setStatus(t);
		} finally {
			try {
				reportStat(zuulContext, start);
			} catch (Throwable t) {
				Cat.logError("ZuulCallable collect stats error.", t);
			}
			try {
				ctx.complete();
			} catch (Throwable t) {
				Cat.logError("AsyncContext complete error.", t);
			}
			zuulContext.unset();

			tran.complete();
		}
		return null;
	}

	private void service(ServletRequest req, ServletResponse res) {
		try {

			init((HttpServletRequest) req, (HttpServletResponse) res);

			// marks this request as having passed through the "Zuul engine", as
			// opposed to servlets
			// explicitly bound in web.xml, for which requests will not have the
			// same data attached
			RequestContext.getCurrentContext().setZuulEngineRan();

			try {
				preRoute();
			} catch (ZuulException e) {
				error(e);
				postRoute();
				return;
			}
			try {
				route();
			} catch (ZuulException e) {
				error(e);
				postRoute();
				return;
			}
			try {
				postRoute();
			} catch (ZuulException e) {
				error(e);
				return;
			}

		} catch (Throwable e) {
			error(new ZuulException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()));
		}
	}

	/**
	 * executes "post" ZuulFilters
	 *
	 * @throws ZuulException
	 */
	private void postRoute() throws ZuulException {
		Transaction tran = Cat.getProducer().newTransaction("ZuulCallable", "postRoute");
		try {
			zuulRunner.postRoute();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * executes "route" filters
	 *
	 * @throws ZuulException
	 */
	private void route() throws ZuulException {
		Transaction tran = Cat.getProducer().newTransaction("ZuulCallable", "route");
		try {
			zuulRunner.route();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * executes "pre" filters
	 *
	 * @throws ZuulException
	 */
	private void preRoute() throws ZuulException {
		Transaction tran = Cat.getProducer().newTransaction("ZuulCallable", "preRoute");
		try {
			zuulRunner.preRoute();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * initializes request
	 *
	 * @param servletRequest
	 * @param servletResponse
	 */
	private void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		zuulRunner.init(servletRequest, servletResponse);
	}

	/**
	 * sets error context info and executes "error" filters
	 *
	 * @param e
	 */
	private void error(ZuulException e) {
		Transaction tran = Cat.getProducer().newTransaction("ZuulCallable", "postRoute");
		try {
			RequestContext.getCurrentContext().setThrowable(e);
			zuulRunner.error();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable t) {
			Cat.logError(t);
		} finally {
			tran.complete();
			Cat.logError(e);
		}
	}

	private void reportStat(RequestContext zuulContext, long start) {

		long remoteServiceCost = 0l;
		Object remoteCallCost = zuulContext.get("remoteCallCost");
		if (remoteCallCost != null) {
			try {
				remoteServiceCost = Long.parseLong(remoteCallCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientCost = 0l;
		Object sendResponseCost = zuulContext.get("sendResponseCost");
		if (sendResponseCost != null) {
			try {
				replyClientCost = Long.parseLong(sendResponseCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientReadCost = 0L;
		Object sendResponseReadCost = zuulContext.get("sendResponseCost:read");
		if (sendResponseReadCost != null) {
			try {
				replyClientReadCost = Long.parseLong(sendResponseReadCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientWriteCost = 0L;
		Object sendResponseWriteCost = zuulContext.get("sendResponseCost:write");
		if (sendResponseWriteCost != null) {
			try {
				replyClientWriteCost = Long.parseLong(sendResponseWriteCost.toString());
			} catch (Exception ignore) {
			}
		}

		if (zuulContext.sendZuulResponse()) {
			URL routeUrl = zuulContext.getRouteUrl();
			if (routeUrl == null) {
				LOGGER.warn("Unknown Route: [ {" + zuulContext.getRequest().getRequestURL() + "} ]");
			}
		}

		// TODO report metrics
	}
}
