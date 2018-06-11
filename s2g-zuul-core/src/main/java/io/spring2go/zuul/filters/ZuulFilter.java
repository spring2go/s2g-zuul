package io.spring2go.zuul.filters;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import io.spring2go.zuul.common.ExecutionStatus;
import io.spring2go.zuul.common.ZuulFilterResult;
import io.spring2go.zuul.common.IZuulFilter;
import io.spring2go.zuul.context.RequestContext;

/**
 * Base abstract class for ZuulFilters. The base class defines abstract methods
 * to define: filterType() - to classify a filter by type. Standard types in
 * Zuul Gateway are "pre" for pre-routing filtering, "route" for routing to an
 * origin, "post" for post-routing filters, "error" for error handling. We also
 * support a "static" type for static responses see StaticResponseFilter. Any
 * filterType made be created or added and run by calling
 * FilterProcessor.runFilters(type)
 * <p/>
 * filterOrder() must also be defined for a filter. Filters may have the same
 * filterOrder if precedence is not important for a filter. filterOrders do not
 * need to be sequential.
 * <p/>
 * ZuulFilters may be disabled using Archius Properties.
 * <p/>
 * By default ZuulFilters are static; they don't carry state. This may be
 * overridden by overriding the isStaticFilter() property to false
 *
 */
public abstract class ZuulFilter implements IZuulFilter, Comparable<ZuulFilter> {

	private final DynamicBooleanProperty filterDisabled = DynamicPropertyFactory.getInstance()
			.getBooleanProperty(disablePropertyName(), false);

	/**
	 * to classify a filter by type. Standard types in Zuul are "pre" for
	 * pre-routing filtering, "route" for routing to an origin, "post" for
	 * post-routing filters, "error" for error handling. We also support a
	 * "static" type for static responses see StaticResponseFilter. Any
	 * filterType made be created or added and run by calling
	 * FilterProcessor.runFilters(type)
	 *
	 * @return A String representing that type
	 */
	abstract public String filterType();

	/**
	 * filterOrder() must also be defined for a filter. Filters may have the
	 * same filterOrder if precedence is not important for a filter.
	 * filterOrders do not need to be sequential.
	 *
	 * @return the int order of a filter
	 */
	abstract public int filterOrder();

	/**
	 * By default ZuulFilters are static; they don't carry state. This may be
	 * overridden by overriding the isStaticFilter() property to false
	 *
	 * @return true by default
	 */
	public boolean isStaticFilter() {
		return true;
	}

	/**
	 * The name of the Archaius property to disable this filter. by default it
	 * is zuul.[classname].[filtertype].disable
	 *
	 * @return
	 */
	public String disablePropertyName() {
		return "zuul." + this.getClass().getSimpleName() + "." + filterType() + ".disable";
	}

	/**
	 * If true, the filter has been disabled by archaius and will not be run
	 *
	 * @return
	 */
	public boolean isFilterDisabled() {
		return filterDisabled.get();
	}

	/**
	 * runFilter checks !isFilterDisabled() and shouldFilter(). The run() method
	 * is invoked if both are true.
	 *
	 * @return the return from ZuulFilterResult
	 */
	public ZuulFilterResult runFilter() {
		ZuulFilterResult tr = new ZuulFilterResult();

		if (!filterDisabled.get()) {		    
			if (shouldFilter()) {
				try {
					Object res = run();
					tr.setStatus(ExecutionStatus.SUCCESS);
					tr.setResult(res);
				} catch (Throwable t) {
					tr.setException(t);
					tr.setStatus(ExecutionStatus.FAILED);
				}
			} else {
				tr.setStatus(ExecutionStatus.SKIPPED);
			}
		}

		return tr;
	}

	public int compareTo(ZuulFilter filter) {
		return this.filterOrder() - filter.filterOrder();
	}
}
