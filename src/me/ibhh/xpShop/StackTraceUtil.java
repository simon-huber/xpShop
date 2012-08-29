package me.ibhh.xpShop;

import java.io.*;

/**
 * Simple utilities to return the stack trace of an exception as a String.
 */
public final class StackTraceUtil {

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Defines a custom format for the stack trace as String.
     */
    public static String getCustomStackTrace(Throwable aThrowable) {
        if (aThrowable != null) {
            //add the class name and any message passed to constructor
            final StringBuilder result = new StringBuilder("");
            result.append(aThrowable.toString());
            final String NEW_LINE = System.getProperty("<br>");
            result.append(NEW_LINE);

            //add each element of the stack trace
            for (StackTraceElement element : aThrowable.getStackTrace()) {
                result.append(element);
                result.append(NEW_LINE);
            }
            return result.toString();
        } else {
            return "no stack trace";
        }
    }
}
