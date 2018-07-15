package io.spring2go.tools.common;



public abstract class AbstractHandler implements StatsHandler {
    @Override
    public void process(Stats stats) {
        if(stats == null) return;

        if(stats instanceof ClassStats) processClassStats((ClassStats) stats);
        else if(stats instanceof ThreadStats) processThreadStats((ThreadStats) stats);
        else if(stats instanceof MemoryStats) processMemoryStats((MemoryStats) stats);
        else if(stats instanceof GCStats) processGCStats((GCStats) stats);
        else if(stats instanceof OperatingSystemStats) processOperatingSystemStats((OperatingSystemStats) stats);
    }

    protected abstract void processGCStats(GCStats stats);

    protected abstract void processMemoryStats(MemoryStats stats);

    protected abstract void processThreadStats(ThreadStats stats);

    protected abstract void processClassStats(ClassStats stats);

    protected abstract void processOperatingSystemStats(OperatingSystemStats stats);
}
