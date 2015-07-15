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
}
