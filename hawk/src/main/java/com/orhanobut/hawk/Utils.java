package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
final class Utils {

  static boolean hasRxJavaOnClasspath() {
    try {
      Class.forName("rx.Observable");
      return true;
    } catch (ClassNotFoundException ignored) {
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
