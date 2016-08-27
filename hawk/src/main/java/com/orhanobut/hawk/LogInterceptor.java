package com.orhanobut.hawk;

/**
 * Interceptor for all logs happens in the library
 */
public interface LogInterceptor {

  /**
   * Will be triggered each time when a log is written
   *
   * @param message is the log message
   */
  void onLog(String message);
}
