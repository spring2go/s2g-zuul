package io.spring2go.zuul.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTag;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;
import io.spring2go.zuul.common.INamedCount;

public class RouteStatusCodeMonitor implements INamedCount {

    String route_code;
    String route;
    int status_code;

    @MonitorTags
    TagList tagList;
    @Monitor(name = "count", type = DataSourceType.COUNTER)
    protected final AtomicLong count = new AtomicLong();

    public RouteStatusCodeMonitor(String route, int status_code) {
        if (route == null) route = "";
        this.route = route;
        this.status_code = status_code;
        route_code = route + "_" + status_code;
        tagList = BasicTagList.of(new BasicTag("ID", route_code));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteStatusCodeMonitor statsData = (RouteStatusCodeMonitor) o;

        if (status_code != statsData.status_code) return false;
        if (route != null ? !route.equals(statsData.route) : statsData.route != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = route != null ? route.hashCode() : 0;
        result = 31 * result + status_code;
        return result;
    }

    @Override
    public String getName() {
        return route_code;
    }

    public long getCount() {
        return count.get();
    }

    /**
     * increment the count
     */
    public void update() {
        count.incrementAndGet();
    }
}
