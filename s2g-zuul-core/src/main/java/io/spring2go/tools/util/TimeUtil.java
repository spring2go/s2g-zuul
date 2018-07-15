package io.spring2go.tools.util;

public class TimeUtil {

    public static String readableTimeInterval(long uptime) {
        long year = uptime/(1000*60*60*24*365);
        long month = (uptime%(1000*60*60*24*365))/(1000*60*60*24*30);
        long day = ((uptime%(1000*60*60*24*365))%(1000*60*60*24*30))/(1000*60*60*24);
        long hour = (uptime%(1000*60*60*24))/(1000*60*60);
        long minute = (uptime%(1000*60*60))/(1000*60);
        long second = (uptime%(1000*60))/(1000);

        StringBuilder builder = new StringBuilder(" ");
        if(year > 0) builder.append(year).append(year>1?" Years " : " Year ");
        if(month > 0) builder.append(month).append(month>1?" Months " : " Month ");
        if(day > 0) builder.append(day).append(day>1?" Days " : " Day ");
        if(hour > 0) builder.append(hour).append(hour>1?" Hours " : " Hour ");
        if(minute > 0) builder.append(minute).append(minute>1?" Minutes " : " Minute ");
        if(second > 0) builder.append(second).append(second>1?" Seconds " : " Second ");

        return builder.toString();
    }
}
