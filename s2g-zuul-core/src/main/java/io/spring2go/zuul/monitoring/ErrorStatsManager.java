package io.spring2go.zuul.monitoring;

import io.spring2go.zuul.context.RequestContext;
import io.spring2go.zuul.monitoring.MonitorRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Manager to handle Error Statistics
 */
public class ErrorStatsManager {
    ConcurrentHashMap<String, ConcurrentHashMap<String, ErrorStatsData>> routeMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ErrorStatsData>>();
    final static ErrorStatsManager INSTANCE = new ErrorStatsManager();

    /**
     *
     * @return Singleton
     */
    public static ErrorStatsManager getManager() {
        return INSTANCE;
    }


    /**
     *
     * @param route
     * @param cause
     * @return data structure for holding count information for a route and cause
     */
    public ErrorStatsData getStats(String route, String cause) {
        Map<String, ErrorStatsData> map = routeMap.get(route);
        if (map == null) return null;
        return map.get(cause);
    }

    /**
     * updates count for the given route and error cause
     * @param route
     * @param cause
     */
    public void putStats(String route, String cause) {
        if (route == null) route = RequestContext.getCurrentContext().getRequest().getRequestURI();
        if (route == null) route = "UNKNOWN_ROUTE";
        route = route.replace("/", "_");
        ConcurrentHashMap<String, ErrorStatsData> statsMap = routeMap.get(route);
        if (statsMap == null) {
            statsMap = new ConcurrentHashMap<String, ErrorStatsData>();
            routeMap.putIfAbsent(route, statsMap);
        }
        ErrorStatsData sd = statsMap.get(cause);
        if (sd == null) {
            sd = new ErrorStatsData(route, cause);
            ErrorStatsData sd1 = statsMap.putIfAbsent(cause, sd);
            if (sd1 != null) {
                sd = sd1;
            } else {
                MonitorRegistry.getInstance().registerObject(sd);
            }
        }
        sd.update();
    }


    @RunWith(MockitoJUnitRunner.class)
    public static class UnitTest {

        @Test
        public void testPutStats() {
            ErrorStatsManager sm = new ErrorStatsManager();
            assertNotNull(sm);
            sm.putStats("test", "cause");
            assertNotNull(sm.routeMap.get("test"));
            ConcurrentHashMap<String, ErrorStatsData> map = sm.routeMap.get("test");
            ErrorStatsData sd = map.get("cause");
            assertEquals(sd.count.get(), 1);
            sm.putStats("test", "cause");
            assertEquals(sd.count.get(), 2);
        }


        @Test
        public void testGetStats() {
            ErrorStatsManager sm = new ErrorStatsManager();
            assertNotNull(sm);
            sm.putStats("test", "cause");
            assertNotNull(sm.getStats("test", "cause"));
        }

    }


}

