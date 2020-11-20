package com.hiro.bytecode_slimming

/**
 * log 工具类
 */
class Logger {
    /* log 信息级别：1 */
    static final int LOG_LEVEL_1 = 1
    /* log 信息级别：2 */
    static final int LOG_LEVEL_2 = 2
    /* log 信息级别：3 */
    static final int LOG_LEVEL_3 = 3
    /* 不让任何级别的 log 信息输出 */
    static final int LOG_LEVEL_NO_LOG = 4

    static int currentLogLevel = LOG_LEVEL_1

    static void setLogLevel(int logLevel) {
        logLevel = Math.max(LOG_LEVEL_1, Math.min(LOG_LEVEL_NO_LOG, logLevel))
        currentLogLevel = logLevel
    }

    static void d1(String tag, String msg) {
        d1(tag, msg, null)
    }

    static void d1(String tag, String msg, Throwable t) {
        d(tag, msg, t, LOG_LEVEL_1)
    }

    static void d2(String tag, String msg) {
        d2(tag, msg, null)
    }

    static void d2(String tag, String msg, Throwable t) {
        d(tag, msg, t, LOG_LEVEL_2)
    }

    static void d3(String tag, String msg) {
        d3(tag, msg, null)
    }

    static void d3(String tag, String msg, Throwable t) {
        d(tag, msg, t, LOG_LEVEL_3)
    }

    static void e(String tag, String msg, Throwable t) {
        System.err.println "$tag: $msg"
        if (t != null) {
            t.printStackTrace()
        }
    }

    private static void d(String tag, String msg, Throwable t, int logLevel) {
        if (logLevel < currentLogLevel) {
            return
        }
        println "$tag: $msg"
        if (t != null) {
            t.printStackTrace()
        }
    }
}