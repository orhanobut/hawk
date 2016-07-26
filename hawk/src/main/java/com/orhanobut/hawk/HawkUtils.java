package com.orhanobut.hawk;

final class HawkUtils {

  private HawkUtils() {
    //no instance
  }

  static boolean hasRxJavaOnClasspath() {
    try {
      Class.forName("rx.Observable");
      return true;
    } catch (ClassNotFoundException ignored) {
      Logger.e("something happened", ignored);
    }
    return false;
  }

  public static void checkRx() {
    if (!hasRxJavaOnClasspath()) {
      throw new NoClassDefFoundError("RxJava is not on classpath, " +
              "make sure that you have it in your dependencies");
    }
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
