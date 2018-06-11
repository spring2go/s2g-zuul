package io.spring2go.zuul.monitoring;

import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.FileMetricObserver;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sample poller to poll metrics using Servo's metric publication
 */
public class MetricPoller {

    private static Logger LOG = LoggerFactory.getLogger(MetricPoller.class);

    final static PollScheduler scheduler = PollScheduler.getInstance();

    public static void startPoller(){
        scheduler.start();
        final int heartbeatInterval = 1200;

        final File metricsDir;
        try {
            metricsDir = File.createTempFile("zuul-servo-metrics-", "");
            metricsDir.delete();
            metricsDir.mkdir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOG.debug("created metrics dir " + metricsDir.getAbsolutePath());

        MetricObserver transform = new CounterToRateMetricTransform(
                new FileMetricObserver("ZuulMetrics", metricsDir),
                heartbeatInterval, TimeUnit.SECONDS);

        PollRunnable task = new PollRunnable(
                new MonitorRegistryMetricPoller(),
                BasicMetricFilter.MATCH_ALL,
                transform);

        final int samplingInterval = 10;
        scheduler.addPoller(task, samplingInterval, TimeUnit.SECONDS);

    }

}