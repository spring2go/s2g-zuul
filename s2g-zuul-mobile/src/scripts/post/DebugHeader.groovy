package scripts.post

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.util.Pair
import io.spring2go.zuul.context.RequestContext
import io.spring2go.zuul.filters.ZuulFilter


class DebugHeader extends ZuulFilter {
    static final DynamicBooleanProperty INCLUDE_DEBUG_HEADER =
            DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.include.debug.header", false);

    static final DynamicBooleanProperty INCLUDE_ROUTE_URL_HEADER =
            DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.include-route-url-header", true);

    @Override
    String filterType() {
        return 'post'
    }

    @Override
    int filterOrder() {
        return 10
    }

    @Override
    boolean shouldFilter() {
        return INCLUDE_DEBUG_HEADER.get();
    }

    @Override
    Object run() {
        addStandardResponseHeaders(RequestContext.getCurrentContext().getRequest(), RequestContext.getCurrentContext().getResponse())
        return null;
    }

    void addStandardResponseHeaders(HttpServletRequest req, HttpServletResponse res) {

        RequestContext context = RequestContext.getCurrentContext()
        List<Pair<String, String>> headers = context.getZuulResponseHeaders()
        headers.add(new Pair("X_ZUUL", "mobile_gateway"))
        // TODO, get zuul instance id
        headers.add(new Pair("CONNECTION", "KEEP_ALIVE"))
        headers.add(new Pair("X_ZUUL_FILTER_EXECUTION_STATUS", context.getFilterExecutionSummary().toString()))
        headers.add(new Pair("X_ORIGINATING_URL", originatingURL))
        if (INCLUDE_ROUTE_URL_HEADER.get()) {
            String routeUrl = context.getRouteUrl();
            if (routeUrl != null && !routeUrl.empty) {
                headers.add(new Pair("x-zuul-route-url", routeUrl));
            }
        }

        //Support CROS
        headers.add(new Pair("Access-Control-Allow-Origin", "*"))
        headers.add(new Pair("Access-Control-Allow-Headers","Content-Type, Accept"))

        headers.add(new Pair("x-zuul-remote-call-cost", String.valueOf(RequestContext.getCurrentContext().get("remoteCallCost"))))

        if (!context.errorHandled() && context.responseStatusCode >= 400) {
            headers.add(new Pair("X_ZUUL_ERROR_CAUSE", "Error from Origin"))
            
        }

        if (INCLUDE_DEBUG_HEADER.get()) {
            String debugHeader = "";
            List<String> rd = (List<String>) context.get("routingDebug");
            rd?.each { debugHeader += "[[[${it}]]]"; }

            headers.add(new Pair("X-Zuul-Debug-Header", debugHeader));
        }
    }

    String getOriginatingURL() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();

        String protocol = request.getHeader("X_FORWARDED_PROTO")
        if (protocol == null) protocol = "http"
        String host = request.getHeader("HOST")
        String uri = request.getRequestURI();
        def URL = "${protocol}://${host}${uri}"
        if (request.getQueryString() != null) {
            URL += "?${request.getQueryString()}"
        }
        return URL
    }

  
}
