package io.spring2go.zuul.common;

import io.spring2go.zuul.filters.ZuulFilter;

/**
 * Interface to implement for registering a callback for each time a filter
 * is used.
 *
 */
public interface IFilterUsageNotifier {
    public void notify(ZuulFilter filter, ExecutionStatus status);
}
