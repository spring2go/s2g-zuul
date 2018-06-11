package scripts.post

import io.spring2go.zuul.context.RequestContext
import io.spring2go.zuul.filters.ZuulFilter
import io.spring2go.zuul.monitor.StatManager

class Stats extends ZuulFilter {
    @Override
    String filterType() {
        return "post"
    }

    @Override
    int filterOrder() {
        return 20000
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        int status = ctx.getResponseStatusCode();
        StatManager sm = StatManager.manager
        sm.collectRequestStats(ctx.getRequest());
        sm.collectRouteStatusStats(ctx.routeName, status);
    }

}
