package scripts.post

import io.spring2go.zuul.common.ZuulException;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;

public class AddTimeStamp extends ZuulFilter {

	private final static int TIMESTAMP_EXPIRED_SECS = 600;

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public boolean shouldFilter() {		
		return true;
	}
	@Override
	public int filterOrder() {
		return 1;

	}
	@Override
	public Object run() throws ZuulException {
		long time = System.currentTimeMillis()/1000;
		RequestContext.getCurrentContext().addZuulResponseHeader("X-S2G-TIMESTAMP", String.valueOf(time));
		return true;
	}
}
