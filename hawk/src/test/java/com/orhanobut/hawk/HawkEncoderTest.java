package com.orhanobut.hawk;

import com.google.gson.Gson;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class HawkEncoderTest extends TestCase {

  private final Encoder encoder;
  private final Parser parser;

  public HawkEncoderTest() {
    parser = new GsonParser(new Gson());
    encoder = new HawkEncoder(parser);
  }

  static class Foo {
    String name = "hawk";
  }

  @Test public void createInstanceWithInvalidValues() {
    try {
      new HawkEncoder(null);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Parser should not be null");
    }
  }

  @Test public void encodeInvalidValues() {
    assertThat(encoder.encode(null)).isNull();
  }

  @Test public void encodeString() {
    String text = "text";
    byte[] expected = parser.toJson(text).getBytes();
    byte[] actual = encoder.encode(text);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeCustomObject() {
    Foo data = new Foo();
    byte[] expected = parser.toJson(data).getBytes();
    byte[] actual = encoder.encode(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeList() {
    List<String> data = new ArrayList<>();
    data.add("test");
    byte[] expected = parser.toJson(data).getBytes();
    byte[] actual = encoder.encode(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeMap() {
    Map<String, String> data = new HashMap<>();
    data.put("key", "value");
    byte[] expected = parser.toJson(data).getBytes();
    byte[] actual = encoder.encode(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void encodeSet() {
    Set<String> data = new HashSet<>();
    data.add("key");
    byte[] expected = parser.toJson(data).getBytes();
    byte[] actual = encoder.encode(data);

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void decodeInvalidValues() throws Exception {
    assertThat(encoder.decode(null, null)).isNull();
    try {
      assertThat(encoder.decode(new byte[34], null)).isNull();
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("data info should not be null");
    }
  }

  @Test public void decodeObject() throws Exception {
    String clazz = "java.lang.String";
    String info = "00V";
    String cipher = "cipher";
    DataInfo dataInfo = DataHelper.getDataInfo(clazz + "##" + info + "@" + cipher);
    String actual = encoder.decode(cipher.getBytes(), dataInfo);
    assertThat(actual).isEqualTo(cipher);
  }

}
