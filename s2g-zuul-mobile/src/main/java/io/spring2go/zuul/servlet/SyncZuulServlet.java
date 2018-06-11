package io.spring2go.zuul.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.core.ZuulRunner;

public class SyncZuulServlet extends HttpServlet {
	
	private static final long serialVersionUID = -7314825620092836092L;

	private static Logger LOGGER = LoggerFactory.getLogger(SyncZuulServlet.class);
	
    private ZuulRunner zuulRunner = new ZuulRunner();

    @Override
    public void service(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws javax.servlet.ServletException, java.io.IOException {
        try {


            init((HttpServletRequest) req, (HttpServletResponse) res);

            // marks this request as having passed through the "Zuul engine", as opposed to servlets
            // explicitly bound in web.xml, for which requests will not have the same data attached
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
        } finally {
        	RequestContext.getCurrentContext().unset();
        }
    }

    /**
     * executes "post" ZuulFilters
     *
     * @throws ZuulException
     */
    void postRoute() throws ZuulException {
    	zuulRunner.postRoute();
    }

    /**
     * executes "route" filters
     *
     * @throws ZuulException
     */
    void route() throws ZuulException {
    	zuulRunner.route();
    }

    /**
     * executes "pre" filters
     *
     * @throws ZuulException
     */
    void preRoute() throws ZuulException {
    	zuulRunner.preRoute();
    }

    /**
     * initializes request
     *
     * @param servletRequest
     * @param servletResponse
     */
    void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    	zuulRunner.init(servletRequest, servletResponse);
    }

	/**
	 * sets error context info and executes "error" filters
	 *
	 * @param e
	 * @throws ZuulException
	 */
	void error(ZuulException e) {
		try {
			RequestContext.getCurrentContext().setThrowable(e);
			zuulRunner.error();
		} catch (Throwable t) {
			Cat.logError(t);
			LOGGER.error(e.getMessage(), e);
		}finally{
			Cat.logError(e);
		}
	}

}
