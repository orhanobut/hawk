package com.orhanobut.hawk;

import android.content.Context;
import android.util.Base64;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;

class ConcealEncryption implements Encryption {

  private final Crypto crypto;

  public ConcealEncryption(Context context) {
    SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
    crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
  }

  @Override public boolean init() {
    return crypto.isAvailable();
  }

  @Override public String encrypt(byte[] value) {
    return null;
  }

  @Override public String encrypt(String key, String plainText) throws Exception {
    Entity entity = Entity.create(key);
    byte[] bytes = crypto.encrypt(plainText.getBytes(), entity);
    return Base64.encodeToString(bytes, Base64.NO_WRAP);
  }

  @Override public byte[] decrypt(String value) {
    return new byte[0];
  }

  @Override public String decrypt(String key, String cipherText) throws Exception {
    Entity entity = Entity.create(key);
    byte[] decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP);
    byte[] bytes = crypto.decrypt(decodedBytes, entity);
    return new String(bytes);
  }

  @Override public boolean reset() {
    return false;
  }

}
