package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
public class HawkSqliteTest extends HawkTest {

  @Override
  public void init() {
    Hawk.init(context)
        .setStorage(HawkBuilder.newSqliteStorage(context))
        .build();
  }
}
