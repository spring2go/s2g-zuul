package io.spring2go.zuul.util;

import io.spring2go.zuul.common.FilterInfo;
import io.spring2go.zuul.filters.FilterScriptManagerServlet;

/**
 * Utility method to build form data for the Admin page for uploading and downloading filters
 * 
 */
public class AdminFilterUtil {

    public static String getState(FilterInfo filter) {
        String state = "inactive";
        if(filter.isActive())state = "active";
        if(filter.isCanary())state = "canary";
        return state;

    }

    public static String buildDeactivateForm(String filter_id, int revision) {
        if (FilterScriptManagerServlet.adminEnabled.get()) {
            return "<form  method=\"POST\" action=\"scriptmanager?action=DEACTIVATE&filter_id=" + filter_id + "&revision=" + revision + "\" >\n" +
                   "<input type=\"submit\" value=\"deactivate\"/></form>";
        } else {
            return "";
        }
    }

    public static String buildActivateForm(String filter_id, int revision) {
        if (FilterScriptManagerServlet.adminEnabled.get()) {
            return "<form  method=\"POST\" action=\"scriptmanager?action=ACTIVATE&filter_id=" + filter_id + "&revision=" + revision + "\" >\n" +
                   "<input type=\"submit\" value=\"activate\"/></form>";
        } else {
            return "";
        }
    }

    public static String buildCanaryForm(String filter_id, int revision) {
        if (FilterScriptManagerServlet.adminEnabled.get()) {
            return "<form  method=\"POST\" action=\"scriptmanager?action=CANARY&filter_id=" + filter_id + "&revision=" + revision + "\" >\n" +
                   "<input type=\"submit\" value=\"canary\"/></form>";
        } else {
            return "";
        }
    }

    public static String buildDownloadLink(String filter_id, int revision) {
        return "<a href=scriptmanager?action=DOWNLOAD&filter_id=" + filter_id + "&revision=" + revision + ">DOWNLOAD</a>";
    }

}
