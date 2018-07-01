package io.spring2go.zuul.common;

import java.util.List;

/**
 * Interface for data access to persist filters in a persistent store
 * 
 */
public interface IZuulFilterDao {
    /**
     *
     * @return a list of all filterIds
     */
    List<String> getAllFilterIds() throws Exception;

    /**
     * returns all filter revisions for the given filterId
     * @param filterId
     * @return returns all filter revisions for the given filterId
     */
    List<FilterInfo> getZuulFilters(String filterId) throws Exception;

    /**
     *
     * @param filterId
     * @param revision
     * @return returns a specific revision for a filter
     */
    FilterInfo getFilter(String filterId, int revision) throws Exception;

    /**
     *
     * @param filterId
     * @return returns the latest version of a given filter
     */
    FilterInfo getLatestFilter(String filterId) throws Exception;

    /**
     * returns the active filter for a given filterId
     * @param filterId
     * @return
     */
    FilterInfo getActiveFilter(String filterId) throws Exception;

    /**
     *
     * @return all filters active in the "canary" mode
     */
    List<FilterInfo> getAllCanaryFilters() throws Exception;

    /**
     *
     * @return all active filters
     */
    List<FilterInfo> getAllActiveFilters() throws Exception;

    /**
     * sets a filter and revision as active in a "canary"
     * @param filterId
     * @param revision
     * @return the filter
     */
    FilterInfo canaryFilter(String filterId, int revision) throws Exception;


    /**
     * sets a filter and revision as active
     * @param filterId
     * @param revision
     * @return the filter
     * @throws Exception
     */
    FilterInfo activateFilter(String filterId, int revision) throws Exception;

    /**
     * Deactiviates a filter; removes it from being active.
     * @param filterId
     * @param revision
     * @return the filter
     * @throws Exception
     */
    FilterInfo deactivateFilter(String filterId, int revision) throws Exception;

    /**
     * adds a new filter to the persistent store
     * @param filterCode
     * @param filterType
     * @param filterName
     * @param filterDisablePropertyName
     * @param filterOrder
     * @return the filter
     */
    FilterInfo addFilter(String filterCode, String filterType, String filterName, String filterDisablePropertyName, String filterOrder) throws Exception;

    /**
     *
     * @param index
     * @return all filter_ids for a given index as a | delimited list
     */
    String getFilterIdsRaw(String index);

    /**
     *
     * @param index
     * @return returns filter_ids for a given index as a parsed list
     */
    List<String> getFilterIdsIndex(String index);
    
    /**
     * close connection
     */
    void close();

    }
