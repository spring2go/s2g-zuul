package io.spring2go.zuul.common;
/**
 * Time based monitoring metric.
 *
 */
public interface ITracer {

    /**
     * Stops and Logs a time based tracer
     *
     */
    void stopAndLog();

    /**
     * Sets the name for the time based tracer
     *
     * @param name a <code>String</code> value
     */
    void setName(String name);

}