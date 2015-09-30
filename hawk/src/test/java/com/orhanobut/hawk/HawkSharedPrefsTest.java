package com.orhanobut.hawk;

public class HawkSharedPrefsTest extends HawkTest {

  @Override public void init() {
    Hawk.init(context)
        .setStorage(HawkBuilder.newSharedPrefStorage(context))
        .build();
  }
}
