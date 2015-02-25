package com.orhanobut.hawk;

import android.util.Log;

/**
 * @author Orhan Obut
 */
@SuppressWarnings("unused")
final class Logger {

    private static final int CHUNK_SIZE = 4000;

    private static final String TAG = "Hawk";

    static void d(String message) {
        log(Log.DEBUG, message);
    }

    static void e(String message) {
        log(Log.ERROR, message);
    }

    static void w(String message) {
        log(Log.WARN, message);
    }

    static void i(String message) {
        log(Log.INFO, message);
    }

    static void v(String message) {
        log(Log.VERBOSE, message);
    }

    static void wtf(String message) {
        log(Log.ASSERT, message);
    }

    private static void log(int logType, String message) {
        LogLevel logLevel = Hawk.getLogLevel();
        if (logLevel == LogLevel.NONE) {
            return;
        }
        int length = message.length();
        if (length <= CHUNK_SIZE) {
            logChunk(logType, message);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int end = Math.min(length, i + CHUNK_SIZE);
            logChunk(logType, message.substring(i, end));
        }
    }

    private static void logChunk(int logType, String chunk) {
        switch (logType) {
            case Log.ERROR:
                Log.e(TAG, chunk);
                break;
            case Log.INFO:
                Log.i(TAG, chunk);
                break;
            case Log.VERBOSE:
                Log.v(TAG, chunk);
                break;
            case Log.WARN:
                Log.w(TAG, chunk);
                break;
            case Log.ASSERT:
                Log.wtf(TAG, chunk);
                break;
            case Log.DEBUG:
                // Fall through, log debug by default
            default:
                Log.d(TAG, chunk);
                break;
        }
    }
}
