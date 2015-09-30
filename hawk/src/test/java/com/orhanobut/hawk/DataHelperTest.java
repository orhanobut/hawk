package com.orhanobut.hawk;

import android.util.Base64;

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
public class DataHelperTest extends TestCase {

  private static final String CIPHER_TEXT = "CIPHER";

  static class Foo {
    String name = "Hawk";
  }

  @Test public void testNewVersionCheck() {
    DataInfo info = DataHelper.getDataInfo("java.lang.String##00V@asdfjasdf");
    assertThat(info.isNewVersion()).isTrue();
  }

  @Test public void testOldVersionCheck() {
    DataInfo info = DataHelper.getDataInfo("java.lang.String##00@asdfjasdf");
    assertThat(info.isNewVersion()).isFalse();
  }

  @Test public void addTypeAsObject() {
    String text = "test";
    String actual = DataHelper.addType(CIPHER_TEXT, text);
    String expected = text.getClass().getName() + "##0V@" + CIPHER_TEXT;
    assertThat(actual).isEqualTo(expected);

    Foo foo = new Foo();
    String actualFoo = DataHelper.addType(CIPHER_TEXT, foo);
    String expectedFoo = foo.getClass().getName() + "##0V@" + CIPHER_TEXT;
    assertThat(actualFoo).isEqualTo(expectedFoo);
  }

  @Test public void addTypeAsList() {
    List<String> list = new ArrayList<>();
    list.add("test");
    String actual = DataHelper.addType(CIPHER_TEXT, list);
    String expected = list.get(0).getClass().getName() + "##1V@" + CIPHER_TEXT;
    assertThat(actual).isEqualTo(expected);

    List<Foo> list2 = new ArrayList<>();
    list2.add(new Foo());
    String actual2 = DataHelper.addType(CIPHER_TEXT, list2);
    String expected2 = list2.get(0).getClass().getName() + "##1V@" + CIPHER_TEXT;
    assertThat(actual2).isEqualTo(expected2);
  }

  @Test public void addTypeAsMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    String actual = DataHelper.addType(CIPHER_TEXT, map);
    String expected = String.class.getName() + "#" +
        String.class.getName() + "#2V@" + CIPHER_TEXT;
    assertThat(actual).isEqualTo(expected);

    Map<String, Foo> map2 = new HashMap<>();
    map2.put("key", new Foo());
    String actual2 = DataHelper.addType(CIPHER_TEXT, map2);
    String expected2 = String.class.getName() + "#" +
        Foo.class.getName() + "#2V@" + CIPHER_TEXT;
    assertThat(actual2).isEqualTo(expected2);
  }

  @Test public void addTypeInvalidValues() {
    try {
      DataHelper.addType(null, null);
    } catch (Exception e) {
      assertThat(e).hasMessage("Cipher text should not be null or empty");
    }
    try {
      DataHelper.addType(null, "234234");
    } catch (Exception e) {
      assertThat(e).hasMessage("Cipher text should not be null or empty");
    }
    try {
      DataHelper.addType("234234", null);
    } catch (Exception e) {
      assertThat(e).hasMessage("Value should not be null");
    }
  }

  @Test public void addTypeAsSet() {
    Set<String> set = new HashSet<>();
    set.add("key");
    String actual = DataHelper.addType(CIPHER_TEXT, set);
    String expected = String.class.getName() + "##3V@" + CIPHER_TEXT;
    assertThat(actual).isEqualTo(expected);

    Set<Foo> set2 = new HashSet<>();
    set2.add(new Foo());
    String actual2 = DataHelper.addType(CIPHER_TEXT, set2);
    String expected2 = Foo.class.getName() + "##3V@" + CIPHER_TEXT;
    assertThat(actual2).isEqualTo(expected2);
  }

  @Test public void getDataInfoAsObject() {
    String clazz = "java.lang.String";
    String info = "00V";
    String cipher = "cipher";
    DataInfo dataInfo = DataHelper.getDataInfo(clazz + "#" + clazz + "#" + info + "@" + cipher);
    assertThat(dataInfo).isNotNull();
    assertThat(dataInfo.isNewVersion()).isTrue();
    assertThat(dataInfo.getKeyClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getValueClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getDataType()).isEqualTo(DataType.OBJECT);
  }

  @Test public void getDataInfoAsList() {
    String clazz = "java.lang.String";
    String info = "1V";
    String cipher = "cipher";
    DataInfo dataInfo = DataHelper.getDataInfo(clazz + "##" + info + "@" + cipher);
    assertThat(dataInfo).isNotNull();
    assertThat(dataInfo.isNewVersion()).isTrue();
    assertThat(dataInfo.getKeyClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getValueClazz()).isNull();
    assertThat(dataInfo.getDataType()).isEqualTo(DataType.LIST);
  }

  @Test public void getDataInfoAsMap() {
    String clazz = "java.lang.String";
    String info = "2V";
    String cipher = "cipher";
    DataInfo dataInfo = DataHelper.getDataInfo(clazz + "#" + clazz + "#" + info + "@" + cipher);
    assertThat(dataInfo).isNotNull();
    assertThat(dataInfo.isNewVersion()).isTrue();
    assertThat(dataInfo.getKeyClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getValueClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getDataType()).isEqualTo(DataType.MAP);
  }

  @Test public void getDataInfoAsSet() {
    String clazz = "java.lang.String";
    String info = "3V";
    String cipher = "cipher";
    DataInfo dataInfo = DataHelper.getDataInfo(clazz + "##" + info + "@" + cipher);
    assertThat(dataInfo).isNotNull();
    assertThat(dataInfo.isNewVersion()).isTrue();
    assertThat(dataInfo.getKeyClazz().getName()).isEqualTo(clazz);
    assertThat(dataInfo.getValueClazz()).isNull();
    assertThat(dataInfo.getDataType()).isEqualTo(DataType.SET);
  }

  @Test public void getDataInfoAsInvalidValues() {
    try {
      DataHelper.getDataInfo(null);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Text should not be null or empty");
    }

    try {
      DataHelper.getDataInfo("");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Text should not be null or empty");
    }

    try {
      DataHelper.getDataInfo("asdfasdf");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Text should contain delimiter");
    }

    try {
      DataHelper.getDataInfo("@234234");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Invalid stored text");
    }

    try {
      DataHelper.getDataInfo("2342423@");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Invalid stored text");
    }
  }

  @Test public void getNewDataInfoWithInvalidValues() {
    try {
      DataHelper.getNewDataInfo(null, null);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
    try {
      DataHelper.getNewDataInfo("", null);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test public void getOldDataInfo() {
    try {
      DataHelper.getOldDataInfo(null, null);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
    try {
      DataHelper.getOldDataInfo("", null);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test public void encodeBase64() {
    String text = "hawk";
    String expected = Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    String actual = DataHelper.encodeBase64(text.getBytes());
    assertThat(actual).isNotNull();
    assertThat(actual).isEqualTo(expected);

    assertThat(DataHelper.encodeBase64(null)).isNull();
  }

  @Test public void decodeBase64() {
    String text = "hawk";
    byte[] expected = Base64.decode(text, Base64.DEFAULT);
    byte[] actual = DataHelper.decodeBase64(text);
    assertThat(actual).isNotNull();
    assertThat(actual).isEqualTo(expected);

    assertThat(DataHelper.decodeBase64(null)).isNull();
  }


}
