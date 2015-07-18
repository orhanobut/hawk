package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
public class HawkEncryptionHighestTest extends HawkTest {

  @Override
  public void init() {
    Hawk.init(context)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
        .setPassword("password")
        .build();
  }
}
