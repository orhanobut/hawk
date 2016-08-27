package com.orhanobut.hawk;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Type;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HawkBuilderTest {

  HawkBuilder builder;
  Context context;

  @Before public void setup() {
    initMocks(this);

    context = RuntimeEnvironment.application;
    builder = new HawkBuilder(context);
  }

  @Test public void testInit() {
    try {
      new HawkBuilder(null);
      fail("Context should not be null");
    } catch (Exception e) {
      assertThat(e).hasMessage("Context should not be null");
    }
  }

  @Test public void testStorage() {
    builder.build();
    assertThat(builder.getStorage()).isInstanceOf(SharedPreferencesStorage.class);

    class MyStorage implements Storage {
      @Override public <T> boolean put(String key, T value) {
        return false;
      }

      @Override public <T> T get(String key) {
        return null;
      }

      @Override public boolean delete(String key) {
        return false;
      }

      @Override public boolean deleteAll() {
        return false;
      }

      @Override public long count() {
        return 0;
      }

      @Override public boolean contains(String key) {
        return false;
      }
    }
    builder.setStorage(new MyStorage()).build();
    assertThat(builder.getStorage()).isInstanceOf(MyStorage.class);
  }

  @Test public void testParser() {
    builder.build();
    assertThat(builder.getParser()).isInstanceOf(GsonParser.class);

    class MyParser implements Parser {

      @Override public <T> T fromJson(String content, Type type) throws Exception {
        return null;
      }

      @Override public String toJson(Object body) {
        return null;
      }
    }
    builder.setParser(new MyParser()).build();
    assertThat(builder.getParser()).isInstanceOf(MyParser.class);
  }

  @Test public void testConverter() {
    builder.build();
    assertThat(builder.getConverter()).isInstanceOf(HawkConverter.class);

    class MyConverter implements Converter {
      @Override public <T> String toString(T value) {
        return null;
      }

      @Override public <T> T fromString(String value, DataInfo dataInfo) throws Exception {
        return null;
      }
    }

    builder.setConverter(new MyConverter()).build();
    assertThat(builder.getConverter()).isInstanceOf(MyConverter.class);
  }

  @Test public void testSerializer() {
    builder.build();
    assertThat(builder.getSerializer()).isInstanceOf(HawkSerializer.class);

    class MySerializer implements Serializer {
      @Override public <T> String serialize(String cipherText, T value) {
        return null;
      }

      @Override public DataInfo deserialize(String plainText) {
        return null;
      }
    }

    builder.setSerializer(new MySerializer()).build();
    assertThat(builder.getSerializer()).isInstanceOf(MySerializer.class);
  }

  @Test public void testEncryption() {
    builder.build();
    assertThat(builder.getEncryption()).isInstanceOf(Base64Encryption.class);

    class MyEncryption implements Encryption {

      @Override public boolean init() {
        return false;
      }

      @Override public String encrypt(String key, String value) throws Exception {
        return null;
      }

      @Override public String decrypt(String key, String value) throws Exception {
        return null;
      }

      @Override public boolean reset() {
        return false;
      }
    }
    builder.setEncryption(new MyEncryption()).build();
    assertThat(builder.getEncryption()).isInstanceOf(MyEncryption.class);
  }

}
