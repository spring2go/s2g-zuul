package io.spring2go.zuul.common;

public interface IMonitor {
    /**
     * Implement this to add this Counter to a Registry
     * @param monitorObj
     */
    void register(INamedCount monitorObj);
}
