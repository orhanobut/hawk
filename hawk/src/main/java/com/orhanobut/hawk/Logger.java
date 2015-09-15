package com.orhanobut.hawk;

import android.util.Log;

@SuppressWarnings("unused")
final class Logger {

  private static final int CHUNK_SIZE = 4000;

  private static final String TAG = "Hawk";

  static void d(String message) {
    log(Log.DEBUG, message, null);
  }

  static void e(String message) {
    log(Log.ERROR, message, null);
  }

  static void e(String message, Throwable throwable) {
    log(Log.ERROR, message, throwable);
  }

  static void w(String message) {
    log(Log.WARN, message, null);
  }

  static void i(String message) {
    log(Log.INFO, message, null);
  }

  static void v(String message) {
    log(Log.VERBOSE, message, null);
  }

  static void wtf(String message) {
    log(Log.ASSERT, message, null);
  }

  private Logger() {
    // no instance
  }

  private static void log(int logType, String message, Throwable throwable) {
    LogLevel logLevel = Hawk.getLogLevel();
    if (logLevel == LogLevel.NONE) {
      return;
    }
    int length = message.length();
    if (length <= CHUNK_SIZE) {
      logChunk(logType, message, throwable);
      return;
    }

    for (int i = 0; i < length; i += CHUNK_SIZE) {
      int end = Math.min(length, i + CHUNK_SIZE);
      logChunk(logType, message.substring(i, end), throwable);
    }
  }

  private static void logChunk(int logType, String chunk, Throwable throwable) {
    switch (logType) {
      case Log.ERROR:
        Log.e(TAG, chunk, throwable);
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