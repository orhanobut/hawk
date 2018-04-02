package com.orhanobut.hawk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HawkBuilderTest {

  private HawkBuilder builder;

  @Before public void setup() {
    initMocks(this);

    builder = new HawkBuilder(RuntimeEnvironment.application);
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

      @Override
      public List<String> getAllKeys() {
        return null;
      }

      @Override
      public long selectiveDelete(List<String> keys) {
        return 0;
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
    assertThat(builder.getEncryption()).isInstanceOf(NoEncryption.class);

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
    }
    builder.setEncryption(new MyEncryption()).build();
    assertThat(builder.getEncryption()).isInstanceOf(MyEncryption.class);
  }

  @Test public void testLogInterceptor() {
    builder.build();
    assertThat(builder.getLogInterceptor()).isInstanceOf(LogInterceptor.class);

    class MyLogInterceptor implements LogInterceptor {
      @Override public void onLog(String message) {

      }
    }
    builder.setLogInterceptor(new MyLogInterceptor()).build();
    assertThat(builder.getLogInterceptor()).isInstanceOf(MyLogInterceptor.class);
  }
}
