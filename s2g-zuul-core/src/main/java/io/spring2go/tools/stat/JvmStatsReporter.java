package io.spring2go.tools.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring2go.tools.common.Stats;
import io.spring2go.tools.common.StatsGetter;
import io.spring2go.tools.common.StatsHandler;


public class JvmStatsReporter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean running = false;
    private Thread workThread;
    private volatile long reportInterval = 1000*60;

    private String name;

    private List<StatsGetter> statsGetterList = new CopyOnWriteArrayList<StatsGetter>();
    private List<StatsHandler> statsHandlerList = new CopyOnWriteArrayList<StatsHandler>();

    public JvmStatsReporter(String name, long interval) {
        this.name = name;
        this.reportInterval = interval;

        statsGetterList.add(new ClassStatsGetter());
        statsGetterList.add(new ThreadStatsGetter());
        statsGetterList.add(new MemoryStatsGetter());
        statsGetterList.add(new GCStatsGetter());
        statsGetterList.add(new OperatingSystemStatsGetter());

        //statsHandlerList.add(new LogHandler());
    }

    synchronized public void start() {
        if (!running) {
            running = true;
            workThread = newWorkThread();
            workThread.start();
        }

    }

    synchronized public void shutdown() {
        if (running) {
            running = false;
            workThread = null;
        }
    }

    public boolean isRunning(){
        return running;
    }

    public void setReportInterval(long reportInterval) {
        if (reportInterval > 0) {
            this.reportInterval = reportInterval;
        }
    }

    private Thread newWorkThread() {
        final String reporterName = this.getClass().getSimpleName();
        return new Thread(reporterName + "-Thread") {
            public void run() {
                try {
                    while (running) {
                        try {
                            for (StatsGetter getter : statsGetterList) {
                                Stats s = getter.get();
                                for (StatsHandler handler : statsHandlerList) {
                                    handler.process(s);
                                }
                            }

                        } catch (Throwable e) {
                            logger.error("Encounter an error while reporting.", e);
                        } finally {
                            sleep(reportInterval);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error(reporterName + " stopped because some error.", e);
                }
            }

        };
    }

    public void addStatsGetter(StatsGetter getter){
        statsGetterList.add(getter);
    }

    public void removeStatsGetter(StatsGetter getter){
        while(statsGetterList.remove(getter));
    }

    public List<StatsGetter> getAllStatsGetter(){
        return new ArrayList<StatsGetter>(statsGetterList);
    }

    public void emptyStatsGetter(){
        statsGetterList.clear();
    }

    public void addStatsHandler(StatsHandler handler){
        statsHandlerList.add(handler);
    }

    public void removeStatsHandler(StatsHandler handler){
        while(statsHandlerList.remove(handler));
    }

    public List<StatsHandler> getAllStatsHandler(){
        return new ArrayList<StatsHandler>(statsHandlerList);
    }

    public void emtyStatsHandler(){
        statsHandlerList.clear();
    }
}
