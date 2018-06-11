package io.spring2go.zuul.common;

import net.jcip.annotations.ThreadSafe;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representation of a ZuulFilter for representing and storing in a database
 */
@ThreadSafe
public class FilterInfo implements  Comparable<FilterInfo>{

    private String filterId;
    private String filterName;
    private String filterCode;
    private String filterType;
    private String filterDisablePropertyName;
    private String filterOrder;
    private String applicationName;
    private int revision;
    private Date createTime;
    /* using AtomicBoolean so we can pass it into EndpointScriptMonitor */
    private final AtomicBoolean isActive = new AtomicBoolean();
    private final AtomicBoolean isCanary = new AtomicBoolean();

    public FilterInfo() {}

    /**
     * Constructor
     * @param filterId
     * @param filterCode
     * @param filterType
     * @param filterName
     * @param disablePropertyName
     * @param filterOrder
     * @param applicationName
     */
    public FilterInfo(String filterId, String filterCode, String filterType, String filterName, String disablePropertyName, String filterOrder, String applicationName) {
        this.filterId = filterId;
        this.filterCode = filterCode;
        this.filterType = filterType;
        this.filterName = filterName;
        this.filterDisablePropertyName = disablePropertyName;
        this.filterOrder = filterOrder;
        this.applicationName = applicationName;
        isActive.set(false);
        isCanary.set(false);
    }

    /**
     *
     * @param filterId
     * @param revision
     * @param createTime
     * @param isActive
     * @param isCanary
     * @param filterCode
     * @param filterType
     * @param filterName
     * @param disablePropertyName
     * @param filterOrder
     * @param applicationName
     */
    public FilterInfo(String filterId, int revision, Date createTime, boolean isActive, boolean isCanary, String filterCode, String filterType, String filterName, String disablePropertyName, String filterOrder, String applicationName) {
        this.filterId = filterId;
        this.revision = revision;
        this.createTime = createTime;
        this.isActive.set(isActive);
        this.isCanary.set(isCanary);
        this.filterCode = filterCode;
        this.filterName = filterName;
        this.filterType = filterType;
        this.filterOrder = filterOrder;
        this.filterDisablePropertyName = disablePropertyName;
        this.applicationName = applicationName;

    }

    /**
     * builds the unique filterId key
     * @param applicationName
     * @param filterType
     * @param filterName
     * @return key is applicationName:filterName:filterType
     */
    public static String buildFilterId(String applicationName, String filterType, String filterName) {
        return applicationName + ":" + filterName + ":" + filterType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterInfo that = (FilterInfo) o;

        if (revision != that.revision) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (filterCode != null ? !filterCode.equals(that.filterCode) : that.filterCode != null) return false;
        if (filterId != null ? !filterId.equals(that.filterId) : that.filterId != null) return false;
        if (filterName != null ? !filterName.equals(that.filterName) : that.filterName != null) return false;
        if (filterType != null ? !filterType.equals(that.filterType) : that.filterType != null) return false;
        if (isActive != null ? !(isActive.get() == that.isActive.get()) : that.isActive != null) return false;
        if (isCanary != null ? !(isCanary.get() == that.isCanary.get()) : that.isCanary != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterId != null ? filterId.hashCode() : 0;
        result = 31 * result + (filterName != null ? filterName.hashCode() : 0);
        result = 31 * result + (filterCode != null ? filterCode.hashCode() : 0);
        result = 31 * result + (filterType != null ? filterType.hashCode() : 0);
        result = 31 * result + revision;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (isCanary != null ? isCanary.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(FilterInfo filterInfo) {
        if(filterInfo.getFilterName().equals(this.getFilterName())){
            return filterInfo.createTime.compareTo(getCreateTime());
        }
        return filterInfo.getFilterName().compareTo(this.getFilterName());
    }

    @Override
    public String toString() {
        return "FilterInfo{" +
                "filterId='" + filterId + '\'' +
                ", filterName='" + filterName + '\'' +
                ", filterType='" + filterType + '\'' +
                ", revision=" + revision +
                ", createTime=" + createTime +
                ", isActive=" + isActive +
                ", isCanary=" + isCanary +
                ", applicationName=" + applicationName +
                '}';
    }

    public String getFilterId() {
        return filterId;
    }

    public void setFilterId(String filterId) {
        this.filterId = filterId;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterDisablePropertyName() {
        return filterDisablePropertyName;
    }

    public void setFilterDisablePropertyName(String filterDisablePropertyName) {
        this.filterDisablePropertyName = filterDisablePropertyName;
    }

    public String getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(String filterOrder) {
        this.filterOrder = filterOrder;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean a) {
        isActive.set(a);
    }

    public boolean isCanary() {
        return isCanary.get();
    }

    public void setCanary(boolean c) {
        isCanary.set(c);
    }
}

