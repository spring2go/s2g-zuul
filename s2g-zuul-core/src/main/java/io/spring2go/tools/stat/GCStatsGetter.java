package io.spring2go.tools.stat;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import io.spring2go.tools.common.GCStats;
import io.spring2go.tools.common.Stats;
import io.spring2go.tools.common.StatsGetter;


public class GCStatsGetter implements StatsGetter {
    String[] youngGenCollectorNames = new String[]{
            // Oracle (Sun) HotSpot
            // -XX:+UseSerialGC
            "Copy",
            // -XX:+UseParNewGC
            "ParNew",
            // -XX:+UseParallelGC
            "PS Scavenge",

            // Oracle (BEA) JRockit
            // -XgcPrio:pausetime
            "Garbage collection optimized for short pausetimes Young Collector",
            // -XgcPrio:throughput
            "Garbage collection optimized for throughput Young Collector",
            // -XgcPrio:deterministic
            "Garbage collection optimized for deterministic pausetimes Young Collector"
    };

    String[] oldGenCollectorNames = new String[]{
            // Oracle (Sun) HotSpot
            // -XX:+UseSerialGC
            "MarkSweepCompact",
            // -XX:+UseParallelGC and (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
            "PS MarkSweep",
            // -XX:+UseConcMarkSweepGC
            "ConcurrentMarkSweep",

            // Oracle (BEA) JRockit
            // -XgcPrio:pausetime
            "Garbage collection optimized for short pausetimes Old Collector",
            // -XgcPrio:throughput
            "Garbage collection optimized for throughput Old Collector",
            // -XgcPrio:deterministic
            "Garbage collection optimized for deterministic pausetimes Old Collector"
    };

    List<String> young = Arrays.asList(youngGenCollectorNames);
    List<String> old = Arrays.asList(oldGenCollectorNames);

    GCStats previous = new GCStats();

    @Override
    public Stats get() {

        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        int minorGcCount=0, fullGcCount=0, otherGcCount=0;
        long minorGcTime=0, fullGcTime=0, otherGcTime=0;
        for (GarbageCollectorMXBean b : beans) {
            String name = b.getName();
            if (young.contains(name)) {
                minorGcCount += b.getCollectionCount();
                minorGcTime += b.getCollectionTime();
            }else if (old.contains(name)) {
                fullGcCount += b.getCollectionCount();
                fullGcTime += b.getCollectionTime();
            }else{
                otherGcCount += b.getCollectionCount();
                otherGcTime += b.getCollectionTime();
            }
        }

        GCStats s = new GCStats();
        s.setMinorGcCount(minorGcCount - previous.getMinorGcCount());
        s.setMinorGcTime(minorGcTime - previous.getMinorGcTime());
        s.setFullGcCount(fullGcCount - previous.getFullGcCount());
        s.setFullGcTime(fullGcTime - previous.getFullGcTime());
        s.setOtherGcCount(otherGcCount - previous.getOtherGcCount());
        s.setOtherGcCount(otherGcTime - previous.getOtherGcTime());

        previous.setMinorGcCount(minorGcCount);
        previous.setMinorGcTime(minorGcTime);
        previous.setFullGcCount(fullGcCount);
        previous.setFullGcTime(fullGcTime);
        previous.setOtherGcCount(otherGcCount);
        previous.setOtherGcCount(otherGcTime);

        return s;
    }
}
