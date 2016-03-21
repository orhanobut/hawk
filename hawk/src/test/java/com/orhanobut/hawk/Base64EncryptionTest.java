package com.orhanobut.hawk;

import android.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class Base64EncryptionTest {

  @Spy Base64Encryption encryption;

  @Before public void setup() {
    encryption = new Base64Encryption();

    initMocks(this);
  }

  @Test public void init() throws Exception {
    assertThat(encryption.init()).isTrue();
  }

  @Test public void encrypt() throws Exception {
    encryption.encrypt(new byte[2]);

    verify(encryption).encodeBase64(any(byte[].class));
  }

  @Test public void decrypt() throws Exception {
    encryption.decrypt("test");

    verify(encryption).decrypt("test");
  }

  @Test public void reset() throws Exception {
    assertThat(encryption.reset()).isTrue();
  }

  @Test public void encodeBase64() {
    String text = "hawk";
    String expected = Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    String actual = encryption.encodeBase64(text.getBytes());

    assertThat(actual).isNotNull();
    assertThat(actual).isEqualTo(expected);
    assertThat(encryption.encodeBase64(null)).isNull();
  }

  @Test public void decodeBase64() {
    String text = "hawk";
    byte[] expected = Base64.decode(text, Base64.DEFAULT);
    byte[] actual = encryption.decodeBase64(text);

    assertThat(actual).isNotNull();
    assertThat(actual).isEqualTo(expected);
    assertThat(encryption.decodeBase64(null)).isNull();
  }
}