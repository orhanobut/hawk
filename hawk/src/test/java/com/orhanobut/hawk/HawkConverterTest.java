package com.orhanobut.hawk;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class HawkConverterTest {

  private Converter converter;
  private Parser parser;
  private Serializer serializer;

  static class Foo {
  }

  @Before public void setup() {
    parser = new GsonParser(new Gson());
    converter = new HawkConverter(parser);
    serializer = new HawkSerializer(new LogInterceptor() {
      @Override public void onLog(String message) {
        // ignore
      }
    });
  }

  @Test public void createInstanceWithInvalidValues() {
    try {
      new HawkConverter(null);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Parser should not be null");
    }
  }

  @Test public void encodeInvalidValues() {
    assertThat(converter.toString(null)).isNull();
  }

  @Test public void encodeString() {
    String text = "text";
    String expected = parser.toJson(text);
    String actual = converter.toString(text);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeCustomObject() {
    Foo data = new Foo();
    String expected = parser.toJson(data);
    String actual = converter.toString(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeList() {
    List<String> data = new ArrayList<>();
    data.add("test");
    String expected = parser.toJson(data);
    String actual = converter.toString(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeMap() {
    Map<String, String> data = new HashMap<>();
    data.put("key", "value");
    String expected = parser.toJson(data);
    String actual = converter.toString(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeSet() {
    Set<String> data = new HashSet<>();
    data.add("key");
    String expected = parser.toJson(data);
    String actual = converter.toString(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void decodeInvalidValues() throws Exception {
    assertThat(converter.fromString(null, null)).isNull();
    try {
      assertThat(converter.fromString("value", null)).isNull();
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("data info should not be null");
    }
  }

  @Test public void decodeObject() throws Exception {
    String clazz = "java.lang.String";
    String info = "00V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "##" + info + "@" + cipher);
    String actual = converter.fromString(cipher, dataInfo);
    assertThat(actual).isEqualTo(cipher);
  }

}
