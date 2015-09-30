package com.orhanobut.hawk;

public class HawkSqliteTest extends HawkTest {

  @Override public void init() {
    Hawk.init(context)
        .setStorage(HawkBuilder.newSqliteStorage(context))
        .build();
  }
}
