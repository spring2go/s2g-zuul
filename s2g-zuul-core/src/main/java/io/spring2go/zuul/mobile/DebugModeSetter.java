package io.spring2go.zuul.mobile;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;

public class DebugModeSetter extends ZuulFilter {

    static final DynamicBooleanProperty couldSetDebug =
            DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.could.set.debug", true);
    static final DynamicBooleanProperty routingDebug =
            DynamicPropertyFactory.getInstance().getBooleanProperty(Constants.ZuulDebugRequest, true);
    static final DynamicStringProperty debugParameter =
            DynamicPropertyFactory.getInstance().getStringProperty(Constants.ZuulDebugParameter, "debugRequest");

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -100;
    }

    public boolean shouldFilter() {
        if (!couldSetDebug.get()) {
            return false;
        }
        System.out.println(RequestContext.getCurrentContext().getRequest().getParameter(debugParameter.get()));
        if ("true".equals(RequestContext.getCurrentContext().getRequest().getParameter(debugParameter.get()))) {
        	
        	return true;
        }
        return routingDebug.get();
    }

    public Object run() {
        RequestContext.getCurrentContext().setDebugRequest(true);
        RequestContext.getCurrentContext().setDebugRouting(true);
        return null;
    }
}



