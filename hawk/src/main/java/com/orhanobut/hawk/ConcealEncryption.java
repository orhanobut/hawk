package com.orhanobut.hawk;

import android.content.Context;

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

  @Override public String encrypt(String key, byte[] value) throws Exception {
    Entity entity = Entity.create(key);
    byte[] encryptedValue = crypto.encrypt(value, entity);
    return new String(encryptedValue);
  }

  @Override public byte[] decrypt(String value) {
    return new byte[0];
  }

  @Override public String decrypt(String key, byte[] value) throws Exception {
    Entity entity = Entity.create(key);
    return new String(crypto.decrypt(value, entity));
  }

  @Override public boolean reset() {
    return false;
  }

}
