package com.orhanobut.hawk;

final class Utils {

  static boolean hasRxJavaOnClasspath() {
    try {
      Class.forName("rx.Observable");
      return true;
    } catch (ClassNotFoundException ignored) {
      Logger.e("something happened", ignored);
    }
    return false;
  }

  private Utils() {
    //no instance
  }

  public static void checkRx() {
    if (!hasRxJavaOnClasspath()) {
      throw new NoClassDefFoundError("RxJava is not on classpath, " +
          "make sure that you have it in your dependencies");
    }
  }
}
