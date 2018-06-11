package io.spring2go.zuul.mobile;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.common.ZuulHeaders;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;

/**
 * Generate a error response while there is an error.
 */
public class ErrorResponse extends ZuulFilter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorResponse.class);

    @Override
    public String filterType() {
        return "error";
    }

    @Override
    public  int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        return context.getThrowable() != null && !context.errorHandled();
    }


    @SuppressWarnings("finally")
	@Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable ex = ctx.getThrowable();
        try {
            String errorCause="Zuul-Error-Unknown-Cause";
            int responseStatusCode;

            if (ex instanceof ZuulException) {
            	ZuulException exception=(ZuulException)ex;
                String cause = exception.errorCause;
                if(cause!=null) errorCause = cause;
                responseStatusCode = exception.nStatusCode;
                
				Enumeration<String> headerIt = ctx.getRequest().getHeaderNames();
				StringBuilder sb = new StringBuilder(ctx.getRequest().getRequestURI()+":"+errorCause);
				while (headerIt.hasMoreElements()) {
					String name = (String) headerIt.nextElement();
					String value = ctx.getRequest().getHeader(name);
					sb.append("REQUEST:: > " + name + ":" + value+"\n");
				}
				logger.error(sb.toString());
            }else{
                responseStatusCode = 500;
            }

            ctx.getResponse().addHeader(ZuulHeaders.X_ZUUL_ERROR_CAUSE, errorCause);
            
			Enumeration<String> headerIt = ctx.getRequest().getHeaderNames();
			StringBuilder sb = new StringBuilder();
			while (headerIt.hasMoreElements()) {
				String name = (String) headerIt.nextElement();
				String value = ctx.getRequest().getHeader(name);
				sb.append("REQUEST:: > " + name + ":" + value+"\n");
			}
			logger.error(sb.toString());

            if (getOverrideStatusCode()) {
                ctx.setResponseStatusCode(200);
            } else {
                ctx.setResponseStatusCode(responseStatusCode);
            }

            ctx.setSendZuulResponse(false);
			ctx.setResponseBody("Message\":\""+errorCause+"\"}");
        } finally {
            ctx.setErrorHandled(true); //ErrorResponse was handled
            return null;
        }
    }
    
   private boolean getOverrideStatusCode() {
        String override = RequestContext.getCurrentContext().getRequest().getParameter("override_error_status");
        if (getCallback() != null) return true;
        if (override == null) return false;
        return Boolean.valueOf(override);

    }

   private String getCallback() {
        String callback = RequestContext.getCurrentContext().getRequest().getParameter("callback");
        if (callback == null) return null;
        return callback;
    }

   private String getOutputType() {
        String output = RequestContext.getCurrentContext().getRequest().getParameter("output");
        if (output == null) return "json";
        return output;
    }  

}