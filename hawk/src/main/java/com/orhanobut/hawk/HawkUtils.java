package com.orhanobut.hawk;

final class HawkUtils {

  private HawkUtils() {
    //no instance
  }

  static void validateBuild() {
    if (!Hawk.isBuilt()) {
      throw new IllegalStateException("Hawk is not built. " +
          "Please call build() and wait the initialisation finishes.");
    }
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
