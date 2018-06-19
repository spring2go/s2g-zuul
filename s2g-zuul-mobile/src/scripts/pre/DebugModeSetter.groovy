package scripts.pre

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty
import io.spring2go.zuul.context.RequestContext
import io.spring2go.zuul.filters.ZuulFilter

class DebugModeSetter extends ZuulFilter {

    static final DynamicBooleanProperty couldSetDebug =
            DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.could.set.debug", true);
    static final DynamicBooleanProperty routingDebug =
            DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.debug.request", false);
    static final DynamicStringProperty debugParameter =
            DynamicPropertyFactory.getInstance().getStringProperty("zuul.debug.parameter", "debugRequest");

    @Override
    String filterType() {
        return 'pre'
    }

    @Override
    int filterOrder() {
        return -100;
    }

    boolean shouldFilter() {
        if (!couldSetDebug.get()) {
            return false
        }
        if ("true".equals(RequestContext.currentContext.getRequest().getParameter(debugParameter.get()))) return true;
        return routingDebug.get();
    }

    Object run() {
        RequestContext.getCurrentContext().setDebugRequest(true)
        RequestContext.getCurrentContext().setDebugRouting(true)
        return null;
    }
}



