package scripts.pre

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat
import com.dianping.cat.message.Transaction
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.filters.ZuulFilter;
import io.spring2go.zuul.util.Debug;

public class DebugRequest extends ZuulFilter {
	
	private static final DynamicBooleanProperty BODY_DEBUG_DISABLED = DynamicPropertyFactory.getInstance().getBooleanProperty("zuul.body.debug.disable", false);


	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return -10;
	}

	@Override
	public boolean shouldFilter() {
		return Debug.debugRequest();
	}

	@Override
	public Object run() {
		
		RequestContext ctx = RequestContext.getCurrentContext()
		Transaction tran = Cat.getProducer().newTransaction("DebugRequestFilter", ctx.getRequest().getRequestURL().toString());

		try {
			HttpServletRequest req = ctx.getRequest();

			Debug.addRequestDebug("REQUEST:: " + req.getScheme() + " " + req.getRemoteAddr() + ":" + req.getRemotePort());
			Debug.addRequestDebug("REQUEST:: > " + req.getMethod() + " " + req.getRequestURI() + " " + req.getProtocol());

			Enumeration<String> headerIt = req.getHeaderNames();
			while (headerIt.hasMoreElements()) {
				String name = (String) headerIt.nextElement();
				String value = req.getHeader(name);
				Debug.addRequestDebug("REQUEST:: > " + name + ":" + value);
			}

			
			if (!ctx.isChunkedRequestBody() && !BODY_DEBUG_DISABLED.get()) {
				InputStream inp = ctx.getRequest().getInputStream();
				String body = null;
				if (inp != null) {
					body = inp.getText();
					Debug.addRequestDebug("REQUEST:: > " + body);
				}
			}
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
		return null;
	}
}
