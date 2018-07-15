package io.spring2go.tools.stat;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import io.spring2go.tools.common.ClassStats;
import io.spring2go.tools.common.Stats;
import io.spring2go.tools.common.StatsGetter;


public class ClassStatsGetter implements StatsGetter {
    ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
    @Override
    public Stats get() {
        ClassStats s = new ClassStats();
        s.setCurrentClassCount(bean.getLoadedClassCount());
        s.setBeenLoadedClassCount(bean.getTotalLoadedClassCount());
        s.setBeenUnloadedClassCount(bean.getUnloadedClassCount());
        return s;
    }
}
