package com.orhanobut.hawk;

public class HawkEncryptionMediumTest extends HawkTest {

  @Override public void init() {
    Hawk.init(context)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
        .build();
  }
}
