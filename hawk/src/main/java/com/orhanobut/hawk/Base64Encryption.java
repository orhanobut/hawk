package com.orhanobut.hawk;

import android.util.Base64;

/**
 * Provides Base64 encoding as non-encryption option.
 * This doesn't provide any encryption
 */
class Base64Encryption implements Encryption {
  @Override public boolean init() {
    return true;
  }

  @Override public String encrypt(byte[] value) {
    return encodeBase64(value);
  }

  @Override public String encrypt(String key, String value) throws Exception {
    return null;
  }


  @Override public byte[] decrypt(String value) {
    return decodeBase64(value);
  }

  @Override public String decrypt(String key, String value) throws Exception {
    return null;
  }

  @Override public boolean reset() {
    return true;
  }

  String encodeBase64(byte[] bytes) {
    try {
      return Base64.encodeToString(bytes, Base64.DEFAULT);
    } catch (Exception e) {
      Logger.w(e.getMessage());
      return null;
    }
  }

  byte[] decodeBase64(String value) {
    try {
      return Base64.decode(value, Base64.DEFAULT);
    } catch (Exception e) {
      Logger.w(e.getMessage());
      return null;
    }
  }
}
