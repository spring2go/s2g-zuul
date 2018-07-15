package io.spring2go.tools.common;



public class ClassStats implements Stats {

    private int currentClassCount;
    private long beenLoadedClassCount;
    private long beenUnloadedClassCount;

    public int getCurrentClassCount() {
        return currentClassCount;
    }

    public void setCurrentClassCount(int currentClassCount) {
        this.currentClassCount = currentClassCount;
    }

    public long getBeenLoadedClassCount() {
        return beenLoadedClassCount;
    }

    public void setBeenLoadedClassCount(long beenLoadedClassCount) {
        this.beenLoadedClassCount = beenLoadedClassCount;
    }

    public long getBeenUnloadedClassCount() {
        return beenUnloadedClassCount;
    }

    public void setBeenUnloadedClassCount(long beenUnloadedClassCount) {
        this.beenUnloadedClassCount = beenUnloadedClassCount;
    }

    @Override
    public String toString() {
        return "\nClassStats{" +
                "\n\tcurrentClassCount=" + currentClassCount +
                ", \n\tbeenLoadedClassCount=" + beenLoadedClassCount +
                ", \n\tbeenUnloadedClassCount=" + beenUnloadedClassCount +
                "\n}";
    }

    @Override
    public String toJsonStr() {
        return "{" +
                "\"currentClassCount\":\"" + currentClassCount +
                "\", \"beenLoadedClassCount\":\"" + beenLoadedClassCount +
                "\", \"beenUnloadedClassCount\":\"" + beenUnloadedClassCount +
                "\"}";
    }
}
