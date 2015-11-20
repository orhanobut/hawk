package com.orhanobut.hawk;

/**
 * Provides Base64 Algorithm
 */
public class Base64Encryption implements Encryption {
  @Override
  public boolean init() {
    return true;
  }

  @Override
  public String encrypt(byte[] value) {
    return DataHelper.encodeBase64(value);
  }

  @Override
  public byte[] decrypt(String value) {
    return DataHelper.decodeBase64(value);
  }

  @Override
  public boolean reset() {
    return true;
  }
}
