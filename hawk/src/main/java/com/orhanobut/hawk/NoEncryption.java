package com.orhanobut.hawk;

import android.util.Base64;

/**
 * Provides Base64 encoding as non-encryption option.
 * This doesn't provide any encryption
 */
public class NoEncryption implements Encryption {
  @Override public boolean init() {
    return true;
  }

  @Override public String encrypt(String key, String value) throws Exception {
    return encodeBase64(value.getBytes());
  }

  @Override public String decrypt(String key, String value) throws Exception {
    return new String(decodeBase64(value));
  }

  String encodeBase64(byte[] bytes) {
    return Base64.encodeToString(bytes, Base64.DEFAULT);
  }

  byte[] decodeBase64(String value) {
    return Base64.decode(value, Base64.DEFAULT);
  }
}
