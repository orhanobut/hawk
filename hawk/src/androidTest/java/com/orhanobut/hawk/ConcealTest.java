package com.orhanobut.hawk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;


@RunWith(AndroidJUnit4.class)
public class ConcealTest {

  Crypto crypto;

  @Before public void setup() {
    Context context = InstrumentationRegistry.getContext();
    SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
    crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
  }

  @Test public void cryptoIsAvailable() {
    assertThat(crypto.isAvailable()).isTrue();
  }

  @Test public void testConceal() throws Exception {
    Entity entity = Entity.create("key");
    String value = "value";
    byte[] encryptedValue = crypto.encrypt(value.getBytes(), entity);

    assertThat(encryptedValue).isNotNull();

    String decryptedValue = new String(crypto.decrypt(encryptedValue, entity));
    assertThat(decryptedValue).isEqualTo("value");
  }
}
