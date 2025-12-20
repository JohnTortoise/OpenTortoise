package io.github.johntortoise.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {


    private static CallerInfo getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();







        for (int i = 3; i < Math.min(stackTrace.length, 10); i++) {
            StackTraceElement element = stackTrace[i];
            if (!element.getClassName().equals(LogUtil.class.getName())) {
                return new CallerInfo(
                        element.getClassName(),
                        element.getMethodName(),
                        element.getLineNumber()
                );
            }
        }


        StackTraceElement caller = stackTrace[3];
        return new CallerInfo(
                caller.getClassName(),
                caller.getMethodName(),
                caller.getLineNumber()
        );
    }


    public static void debug(String format, Object... args) {
        CallerInfo caller = getCallerInfo();
        String message = String.format("[lineNumber:%d] %s",
                caller.lineNumber,
                format(format, args));
        getLogger(caller.className).debug(message);
    }

    public static void info(String format, Object... args) {
        CallerInfo caller = getCallerInfo();
        String message = String.format("[lineNumber:%d] %s",
                caller.lineNumber,
                format(format, args));
        getLogger(caller.className).info(message);
    }

    public static void warn(String format, Object... args) {
        CallerInfo caller = getCallerInfo();
        String message = String.format("[lineNumber:%d] %s",
                caller.lineNumber,
                format(format, args));
        getLogger(caller.className).warn(message);
    }

    public static void error(String format, Object... args) {
        CallerInfo caller = getCallerInfo();
        String message = String.format("[lineNumber:%d] %s",
                caller.lineNumber,
                format(format, args));
        getLogger(caller.className).error(message);
    }

    public static void error(String msg, Throwable t) {
        CallerInfo caller = getCallerInfo();
        String message = String.format("[lineNumber:%d] %s",
                caller.lineNumber,
                msg);
        getLogger(caller.className).error(message, t);
    }



    private static Logger getLogger(String className) {
        return LogManager.getLogger(className);
    }

    private static String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot != -1) {
            return fullClassName.substring(lastDot + 1);
        }
        return fullClassName;
    }

    private static String format(String format, Object... args) {
        format = format.replace("{}","%s");
        if (args == null || args.length == 0) {
            return format;
        }
        return String.format(format, args);
    }


    private static class CallerInfo {
        final String className;
        final String methodName;
        final int lineNumber;

        CallerInfo(String className, String methodName, int lineNumber) {
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
        }
    }
}