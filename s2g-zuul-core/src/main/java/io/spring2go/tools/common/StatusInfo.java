package io.spring2go.tools.common;

public class StatusInfo extends AbstractHandler{
    volatile GCStats gcStats = new GCStats();
    volatile MemoryStats memoryStats = new MemoryStats();
    volatile ThreadStats threadStats = new ThreadStats();
    volatile ClassStats classStats = new ClassStats();
    volatile OperatingSystemStats systemStats = new OperatingSystemStats();

    public GCStats getGCStats(){
    	return this.gcStats;
    }
     
    @Override
    protected void processGCStats(GCStats gcStats) {
        this.gcStats = gcStats;
    }

    @Override
    protected void processMemoryStats(MemoryStats memoryStats) {
        this.memoryStats = memoryStats;
    }

    @Override
    protected void processThreadStats(ThreadStats threadStats) {
        this.threadStats = threadStats;
    }

    @Override
    protected void processClassStats(ClassStats classStats) {
        this.classStats = classStats;
    }

    @Override
    protected void processOperatingSystemStats(OperatingSystemStats operatingSystemStats) {
        this.systemStats = operatingSystemStats;
    }
}
