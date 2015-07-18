package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
public class HawkSharedPrefsTest extends HawkTest {

  @Override
  public void init() {
    Hawk.init(context)
        .setStorage(HawkBuilder.newSharedPrefStorage(context))
        .build();
  }
}
