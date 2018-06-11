package io.spring2go.zuul.monitoring;

import io.spring2go.zuul.common.IMonitor;
import io.spring2go.zuul.common.INamedCount;

public class MonitorRegistry {

    private static  final MonitorRegistry instance = new MonitorRegistry();
    private IMonitor publisher;

    /**
     * A Monitor implementation should be set here
     * @param publisher
     */
    public void setPublisher(IMonitor publisher) {
        this.publisher = publisher;
    }



    public static MonitorRegistry getInstance() {
        return instance;
    }

    public void registerObject(INamedCount monitorObj) {
      if(publisher != null) publisher.register(monitorObj);
    }
}