package io.spring2go.zuul.monitoring;

import com.netflix.servo.monitor.Monitors;
import io.spring2go.zuul.common.IMonitor;
import io.spring2go.zuul.common.INamedCount;

public class ServoMonitor implements IMonitor {
    @Override
    public void register(INamedCount monitorObj) {
        Monitors.registerObject(monitorObj);
    }
}