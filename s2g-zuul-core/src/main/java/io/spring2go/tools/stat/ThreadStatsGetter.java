package io.spring2go.tools.stat;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import io.spring2go.tools.common.Stats;
import io.spring2go.tools.common.StatsGetter;
import io.spring2go.tools.common.ThreadStats;


public class ThreadStatsGetter implements StatsGetter {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    @Override
    public Stats get() {
        ThreadStats s = new ThreadStats();
        s.setCurrentThreadCount(bean.getThreadCount());
        s.setDaemonThreadCount(bean.getDaemonThreadCount());
        s.setBeenCreatedThreadCount(bean.getTotalStartedThreadCount());
        return s;
    }
}
