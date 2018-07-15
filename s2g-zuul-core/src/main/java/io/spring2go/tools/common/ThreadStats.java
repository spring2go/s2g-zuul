package io.spring2go.tools.common;

public class ThreadStats implements Stats {
    //Include daemon and none-daemon
    private int currentThreadCount;

    private int daemonThreadCount;

    private long beenCreatedThreadCount;

    public int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public void setCurrentThreadCount(int currentThreadCount) {
        this.currentThreadCount = currentThreadCount;
    }

    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public long getBeenCreatedThreadCount() {
        return beenCreatedThreadCount;
    }

    public void setBeenCreatedThreadCount(long beenCreatedThreadCount) {
        this.beenCreatedThreadCount = beenCreatedThreadCount;
    }

    @Override
    public String toJsonStr() {
        return "{" +
                "\"currentThreadCount\":\"" + currentThreadCount +
                "\", \"daemonThreadCount\":\"" + daemonThreadCount +
                "\", \"beenCreatedThreadCount\":\"" + beenCreatedThreadCount +
                "\"}";
    }

    @Override
    public String toString() {
        return "\nThreadStats{" +
                "\t\ncurrentThreadCount=" + currentThreadCount +
                ", \t\ndaemonThreadCount=" + daemonThreadCount +
                ", \t\nbeenCreatedThreadCount=" + beenCreatedThreadCount +
                "\n}";
    }
}
