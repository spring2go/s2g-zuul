package io.spring2go.zuul.mobile;

import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;
import io.spring2go.zuul.monitoring.StatManager;

public class Stats extends ZuulFilter {
    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 20000;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        int status = ctx.getResponseStatusCode();
        StatManager sm = StatManager.getManager();
        sm.collectRequestStats(ctx.getRequest());
        sm.collectRouteStatusStats(ctx.getRouteName(), status);
        return null;
    }
}
