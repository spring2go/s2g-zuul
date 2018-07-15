package io.spring2go.tools.util;

public class ByteUtil {

    public static String bytesToSize(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return String.format("%.0f", (double) bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return String.format("%.2f", (double)bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return String.format("%.2f", (double)bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return String.format("%.2f", (double)bytes / terabyte) + " TB";

        } else {
            return bytes + " B";
        }

    }
}
