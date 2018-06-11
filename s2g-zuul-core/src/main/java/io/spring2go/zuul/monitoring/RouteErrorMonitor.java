package io.spring2go.zuul.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTag;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;
import io.spring2go.zuul.common.INamedCount;

/**
 * Implementation of a Named counter to monitor and count error causes by route. Route is a defined zuul gateway concept to
 * categorize requests into buckets. By default this is the first segment of the uri
 *
 */
public class RouteErrorMonitor implements INamedCount {

    String id;
    String error_cause;

    @MonitorTags
    TagList tagList;
    @Monitor(name = "count", type = DataSourceType.COUNTER)
    AtomicLong count = new AtomicLong();

    /**
     * create a counter by route and cause of error
     *
     * @param route
     * @param cause
     */
    public RouteErrorMonitor(String route, String cause) {
        if (null == route || "".equals(route)) {
            route = "UNKNOWN";
        }
        id = route + "_" + cause;

        this.error_cause = cause;
        tagList = BasicTagList.of(new BasicTag("ID", id));


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteErrorMonitor that = (RouteErrorMonitor) o;

        return !(error_cause != null ? !error_cause.equals(that.error_cause) : that.error_cause != null);

    }

    @Override
    public int hashCode() {
        return error_cause != null ? error_cause.hashCode() : 0;
    }

    /**
     * increments the counter
     */
    public void update() {
        count.incrementAndGet();
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public long getCount() {
        return count.get();
    }

}
