package io.spring2go.tools.common;

import io.spring2go.tools.util.ByteUtil;

public class OperatingSystemStats implements Stats {

    private long committedVirtualMemory;
    private long totalSwapSpaceSize;
    private long freeSwapSpaceSize;
    private long processCpuTime;
    private long freePhysicalMemorySize;
    private long totalPhysicalMemorySize;
    private double systemCpuLoad;
    private double processCpuLoad;
    private long maxFileDescriptorCount;
    private long openFileDescriptorCount;

    public long getCommittedVirtualMemory() {
        return committedVirtualMemory;
    }

    public void setCommittedVirtualMemory(long committedVirtualMemory) {
        this.committedVirtualMemory = committedVirtualMemory;
    }

    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
        this.totalSwapSpaceSize = totalSwapSpaceSize;
    }

    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
        this.freeSwapSpaceSize = freeSwapSpaceSize;
    }

    public long getProcessCpuTime() {
        return processCpuTime;
    }

    public void setProcessCpuTime(long processCpuTime) {
        this.processCpuTime = processCpuTime;
    }

    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public long getMaxFileDescriptorCount() {
        return maxFileDescriptorCount;
    }

    public void setMaxFileDescriptorCount(long maxFileDescriptorCount) {
        this.maxFileDescriptorCount = maxFileDescriptorCount;
    }

    public long getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    public void setOpenFileDescriptorCount(long openFileDescriptorCount) {
        this.openFileDescriptorCount = openFileDescriptorCount;
    }

    @Override
    public String toJsonStr() {
        return "{" +
                "\"committedVirtualMemory\":\"" +  ByteUtil.bytesToSize(committedVirtualMemory) +
                "\", \"totalSwapSpaceSize\":\"" +  ByteUtil.bytesToSize(totalSwapSpaceSize) +
                "\", \"freeSwapSpaceSize\":\"" +  ByteUtil.bytesToSize(freeSwapSpaceSize) +
                "\", \"processCpuTimeNS\":\"" + processCpuTime +
                "\", \"freePhysicalMemorySize\":\"" +  ByteUtil.bytesToSize(freePhysicalMemorySize) +
                "\", \"totalPhysicalMemorySize\":\"" +  ByteUtil.bytesToSize(totalPhysicalMemorySize) +
                "\", \"systemCpuLoad\":\"" + String.format("%.4f", systemCpuLoad) +
                "\", \"processCpuLoad\":\"" + String.format("%.4f", processCpuLoad) +
                "\", \"maxFileDescriptorCount \":\"" + maxFileDescriptorCount +
                "\", \"openFileDescriptorCount \":\"" + openFileDescriptorCount +
                "\"}";
    }

    @Override
    public String toString() {
        return "\nOperatingSystemStats{" +
                "\n\tcommittedVirtualMemory=" + committedVirtualMemory +
                ", \n\ttotalSwapSpaceSize=" + totalSwapSpaceSize +
                ", \n\tfreeSwapSpaceSize=" + freeSwapSpaceSize +
                ", \n\tprocessCpuTime=" + processCpuTime +
                ", \n\tfreePhysicalMemorySize=" + freePhysicalMemorySize +
                ", \n\ttotalPhysicalMemorySize=" + totalPhysicalMemorySize +
                ", \n\tsystemCpuLoad=" + systemCpuLoad +
                ", \n\tprocessCpuLoad=" + processCpuLoad +
                ", \n\tmaxFileDescriptorCount =" + maxFileDescriptorCount +
                ", \n\topenFileDescriptorCount =" + openFileDescriptorCount +
                "\n\t}";
    }
}
