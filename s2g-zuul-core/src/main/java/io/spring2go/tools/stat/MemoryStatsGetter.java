package io.spring2go.tools.stat;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import io.spring2go.tools.common.MemoryStats;
import io.spring2go.tools.common.Stats;
import io.spring2go.tools.common.StatsGetter;


public class MemoryStatsGetter implements StatsGetter {
    MemoryMXBean bean = ManagementFactory.getMemoryMXBean();

    @Override
    public Stats get() {
        MemoryStats s = new MemoryStats();

        MemoryUsage u = bean.getHeapMemoryUsage();
        s.setHeapCommitedMemory(u.getCommitted());
        s.setHeapUsedMemory(u.getUsed());
        s.setHeapMaxMemory(u.getMax());

        u = bean.getNonHeapMemoryUsage();
        s.setNonHeapCommitedMemory(u.getCommitted());
        s.setNonHeapUsedMemory(u.getUsed());
        s.setNonHeapMaxMemory(u.getMax());

        return s;
    }
}
