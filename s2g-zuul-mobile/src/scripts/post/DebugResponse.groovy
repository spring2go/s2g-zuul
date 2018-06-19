package scripts.post

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.util.Pair
import io.spring2go.zuul.context.RequestContext
import io.spring2go.zuul.filters.ZuulFilter
import io.spring2go.zuul.util.Debug

class DebugResponse extends ZuulFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugResponse.class);
	
	private static final String ZUUL_BODY_DEBUG_DISABLE = "zuul.body.debug.disable";
	private static final String ZUUL_HEADER_DEBUG_DISABLE = "zuul.header.debug.disable";
	static final DynamicBooleanProperty BODY_DEBUG_DISABLED =
	DynamicPropertyFactory.getInstance().getBooleanProperty(ZUUL_BODY_DEBUG_DISABLE, false);
	static final DynamicBooleanProperty HEADER_DEBUG_DISABLED =
	DynamicPropertyFactory.getInstance().getBooleanProperty(ZUUL_HEADER_DEBUG_DISABLE, true);

    @Override
    String filterType() {
        return 'post'
    }

    @Override
    int filterOrder() {
        return 1000
    }

    @Override
    boolean shouldFilter() {
        return Debug.debugRequest();
    }

    @Override
    Object run() {
        RequestContext.getCurrentContext().getZuulResponseHeaders()?.each { Pair<String, String> it ->
            Debug.addRequestDebug("OUTBOUND: <  " + it.first() + ":" + it.second())
        }
        dumpRoutingDebug()
        dumpRequestDebug()
        return null;
    }

    public void dumpRequestDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("requestDebug");
        StringBuilder b = new StringBuilder("");
        rd?.each {
            b.append("REQUEST_DEBUG::${it}\n");
        }
        LOGGER.info(b.toString());
    }

    public void dumpRoutingDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("routingDebug");
        StringBuilder b = new StringBuilder("");
        rd?.each {
            b.append("ZUUL_DEBUG::${it}\n");
        }
        LOGGER.info(b.toString());
    }
}
