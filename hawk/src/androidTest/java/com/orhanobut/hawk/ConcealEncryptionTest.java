package com.orhanobut.hawk;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class ConcealEncryptionTest {

  Encryption encryption;

  @Before public void setup() {
    encryption = new ConcealEncryption(InstrumentationRegistry.getContext());
  }

  @Test public void init() throws Exception {
    assertThat(encryption.init()).isTrue();
  }

  @Test public void testEncryptAndDecrypt() throws Exception {
    String key = "key";
    String value = "value";

    String cipherText = encryption.encrypt(key, value);

    String plainValue = encryption.decrypt(key, cipherText);

    assertThat(plainValue).isEqualTo(value);
  }
}