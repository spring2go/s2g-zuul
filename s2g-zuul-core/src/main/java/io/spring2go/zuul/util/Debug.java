package io.spring2go.zuul.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.spring2go.zuul.context.RequestContext;

/**
 * Simple wrapper class around the RequestContext for setting and managing Request level Debug data.
 */
public class Debug {

    public static void setDebugRequest(boolean bDebug) {
        RequestContext.getCurrentContext().setDebugRequest(bDebug);
    }

    public static void setDebugRequestHeadersOnly(boolean bHeadersOnly) {
        RequestContext.getCurrentContext().setDebugRequestHeadersOnly(bHeadersOnly);
    }

    public static boolean debugRequestHeadersOnly() {
        return RequestContext.getCurrentContext().debugRequestHeadersOnly();
    }


    public static void setDebugRouting(boolean bDebug) {
        RequestContext.getCurrentContext().setDebugRouting(bDebug);
    }


    public static boolean debugRequest() {
        return RequestContext.getCurrentContext().debugRequest();
    }

    public static boolean debugRouting() {
        return RequestContext.getCurrentContext().debugRouting();
    }

    public static void addRoutingDebug(String line) {
        List<String> rd = getRoutingDebug();
        rd.add(line);
    }

    /**
     *
     * @return Returns the list of routiong debug messages
     */
    public static List<String> getRoutingDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("routingDebug");
        if (rd == null) {
            rd = new ArrayList<String>();
            RequestContext.getCurrentContext().set("routingDebug", rd);
        }
        return rd;
    }

    /**
     * Adds a line to the  Request debug messages
     * @param line
     */
    public static void addRequestDebug(String line) {
        List<String> rd = getRequestDebug();
        rd.add(line);
    }

    /**
     *
     * @return returns the list of request debug messages
     */
    public static List<String> getRequestDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("requestDebug");
        if (rd == null) {
            rd = new ArrayList<String>();
            RequestContext.getCurrentContext().set("requestDebug", rd);
        }
        return rd;
    }


    /**
     * Adds debug details about changes that a given filter made to the request context.
     * @param filterName
     * @param copy
     */
    public static void compareContextState(String filterName, RequestContext copy) {
        RequestContext context = RequestContext.getCurrentContext();
        Iterator<String> it = context.keySet().iterator();
        String key = it.next();
        while (key != null) {
            if ((!key.equals("routingDebug") && !key.equals("requestDebug"))) {
                Object newValue = context.get(key);
                Object oldValue = copy.get(key);
                if (oldValue == null && newValue != null) {
                    addRoutingDebug("{" + filterName + "} added " + key + "=" + newValue.toString());
                } else if (oldValue != null && newValue != null) {
                    if (!(oldValue.equals(newValue))) {
                        addRoutingDebug("{" +filterName + "} changed " + key + "=" + newValue.toString());
                    }
                }
            }
            if (it.hasNext()) {
                key = it.next();
            } else {
                key = null;
            }
        }

    }
}