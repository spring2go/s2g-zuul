package io.spring2go.tools.common;


public class GCStats implements Stats {
    private long minorGcCount;
    private long minorGcTime;

    private long fullGcCount;
    private long fullGcTime;

    private long otherGcCount;
    private long otherGcTime;


    public long getMinorGcCount() {
        return minorGcCount;
    }

    public void setMinorGcCount(long minorGcCount) {
        this.minorGcCount = minorGcCount;
    }

    public long getMinorGcTime() {
        return minorGcTime;
    }

    public void setMinorGcTime(long minorGcTime) {
        this.minorGcTime = minorGcTime;
    }

    public long getFullGcCount() {
        return fullGcCount;
    }

    public void setFullGcCount(long fullGcCount) {
        this.fullGcCount = fullGcCount;
    }

    public long getFullGcTime() {
        return fullGcTime;
    }

    public void setFullGcTime(long fullGcTime) {
        this.fullGcTime = fullGcTime;
    }

    public long getOtherGcCount() {
        return otherGcCount;
    }

    public void setOtherGcCount(long otherGcCount) {
        this.otherGcCount = otherGcCount;
    }

    public long getOtherGcTime() {
        return otherGcTime;
    }

    public void setOtherGcTime(long otherGcTime) {
        this.otherGcTime = otherGcTime;
    }

    @Override
    public String toJsonStr() {
        return "{" +
                "\"minorGcCount\":\"" + minorGcCount +
                "\", \"minorGcTimeMS\":\"" + minorGcTime +
                "\", \"fullGcCount\":\"" + fullGcCount +
                "\", \"fullGcTimeMS\":\"" + fullGcTime +
                "\", \"otherGcCount\":\"" + otherGcCount +
                "\", \"otherGcTime\":\"" + otherGcTime +
                "\"}";
    }

    @Override
    public String toString() {
        return "\nGCStats{" +
                "\n\tminorGcCount=" + minorGcCount +
                ",\n\tminorGcTime=" + minorGcTime +
                ",\n\tfullGcCount=" + fullGcCount +
                ",\n\tfullGcTime=" + fullGcTime +
                ",\n\totherGcCount=" + otherGcCount +
                ",\n\totherGcTime=" + otherGcTime +
                "\n}";
    }
}
