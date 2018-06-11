package io.spring2go.zuul.monitoring;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.InjectableTag;
import com.netflix.servo.tag.Tag;

import io.spring2go.zuul.monitoring.CounterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Plugin to hook up a Servo counter to the CounterFactory
 */
public class Counter extends CounterFactory {
    final static ConcurrentMap<String, BasicCounter> map = new ConcurrentHashMap<String, BasicCounter>();
    final Object lock = new Object();

    @Override
    public void increment(String name) {
        BasicCounter counter = getCounter(name);
        counter.increment();
    }

    private BasicCounter getCounter(String name) {
        BasicCounter counter = map.get(name);
        if (counter == null) {
            synchronized (lock) {
                counter = map.get(name);
                if (counter != null) {
                    return counter;
                }

                List<Tag> tags = new ArrayList<Tag>(2);
                tags.add(InjectableTag.HOSTNAME);
                tags.add(InjectableTag.IP);
                counter = new BasicCounter(MonitorConfig.builder(name).withTags(tags).build());
                map.putIfAbsent(name, counter);
                DefaultMonitorRegistry.getInstance().register(counter);
            }
        }
        return counter;
    }
}