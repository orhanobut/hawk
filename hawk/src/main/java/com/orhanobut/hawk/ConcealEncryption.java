package com.orhanobut.hawk;

import android.content.Context;
import android.util.Base64;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.KeyChain;

class ConcealEncryption implements Encryption {

  private final Crypto crypto;

  public ConcealEncryption(Context context) {
    this(new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256));
  }

  protected ConcealEncryption(KeyChain keyChain) {
    this(AndroidConceal.get().createDefaultCrypto(keyChain));
  }

  protected ConcealEncryption(Crypto crypto) {
    this.crypto = crypto;
  }

  @Override public boolean init() {
    return crypto.isAvailable();
  }

  @Override public String encrypt(String key, String plainText) throws Exception {
    Entity entity = Entity.create(key);
    byte[] bytes = crypto.encrypt(plainText.getBytes(), entity);
    return Base64.encodeToString(bytes, Base64.NO_WRAP);
  }

  @Override public String decrypt(String key, String cipherText) throws Exception {
    Entity entity = Entity.create(key);
    byte[] decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP);
    byte[] bytes = crypto.decrypt(decodedBytes, entity);
    return new String(bytes);
  }

}
