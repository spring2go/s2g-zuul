package io.spring2go.tools.common;

import io.spring2go.tools.util.ByteUtil;

public class MemoryStats implements Stats {
    private long heapUsedMemory;
    private long heapCommitedMemory;
    private long heapMaxMemory;

    private long nonHeapUsedMemory;
    private long nonHeapCommitedMemory;
    private long nonHeapMaxMemory;

    public long getHeapUsedMemory() {
        return heapUsedMemory;
    }

    public void setHeapUsedMemory(long heapUsedMemory) {
        this.heapUsedMemory = heapUsedMemory;
    }

    public long getHeapCommitedMemory() {
        return heapCommitedMemory;
    }

    public void setHeapCommitedMemory(long heapCommitedMemory) {
        this.heapCommitedMemory = heapCommitedMemory;
    }

    public long getHeapMaxMemory() {
        return heapMaxMemory;
    }

    public void setHeapMaxMemory(long heapMaxMemory) {
        this.heapMaxMemory = heapMaxMemory;
    }

    public long getNonHeapUsedMemory() {
        return nonHeapUsedMemory;
    }

    public void setNonHeapUsedMemory(long nonHeapUsedMemory) {
        this.nonHeapUsedMemory = nonHeapUsedMemory;
    }

    public long getNonHeapCommitedMemory() {
        return nonHeapCommitedMemory;
    }

    public void setNonHeapCommitedMemory(long nonHeapCommitedMemory) {
        this.nonHeapCommitedMemory = nonHeapCommitedMemory;
    }

    public long getNonHeapMaxMemory() {
        return nonHeapMaxMemory;
    }

    public void setNonHeapMaxMemory(long nonHeapMaxMemory) {
        this.nonHeapMaxMemory = nonHeapMaxMemory;
    }

    @Override
    public String toJsonStr() {
        return "{" +
                "\"heapUsedMemory\":\"" + ByteUtil.bytesToSize(heapUsedMemory) +
                "\", \"heapCommitedMemory\":\"" + ByteUtil.bytesToSize(heapCommitedMemory) +
                "\", \"heapMaxMemory\":\"" + ByteUtil.bytesToSize(heapMaxMemory) +
                "\", \"nonHeapUsedMemory\":\"" + ByteUtil.bytesToSize(nonHeapUsedMemory) +
                "\", \"nonHeapCommitedMemory\":\"" + ByteUtil.bytesToSize(nonHeapCommitedMemory) +
                "\", \"nonHeapMaxMemory\":\"" + ByteUtil.bytesToSize(nonHeapMaxMemory) +
                "\"}";
    }

    @Override
    public String toString() {
        return "\nMemoryStats{" +
                "\n\theapUsedMemory=" + ByteUtil.bytesToSize(heapUsedMemory) +
                ", \n\theapCommitedMemory=" +  ByteUtil.bytesToSize(heapCommitedMemory) +
                ", \n\theapMaxMemory=" +  ByteUtil.bytesToSize(heapMaxMemory) +
                ", \n\tnonHeapUsedMemory=" +  ByteUtil.bytesToSize(nonHeapUsedMemory) +
                ", \n\tnonHeapCommitedMemory=" +  ByteUtil.bytesToSize(nonHeapCommitedMemory) +
                ", \n\tnonHeapMaxMemory=" +  ByteUtil.bytesToSize(nonHeapMaxMemory) +
                "\n}";
    }
}
