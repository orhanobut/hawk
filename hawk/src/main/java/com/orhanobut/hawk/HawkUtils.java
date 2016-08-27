package com.orhanobut.hawk;

final class HawkUtils {

  private HawkUtils() {
    //no instance
  }

  public static void checkNull(String message, Object value) {
    if (value == null) {
      throw new NullPointerException(message + " should not be null");
    }
  }

  public static void checkNullOrEmpty(String message, String value) {
    if (isEmpty(value)) {
      throw new NullPointerException(message + " should not be null or empty");
    }
  }

  public static boolean isEmpty(String text) {
    return text == null || text.trim().length() == 0;
  }
}
