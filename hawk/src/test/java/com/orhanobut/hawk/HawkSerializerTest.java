package com.orhanobut.hawk;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class HawkSerializerTest {

  private static final String CIPHER_TEXT = "CIPHER";

  Serializer serializer;

  @Before public void setup() {
    serializer = new HawkSerializer();
  }

  static class Foo {
  }

  @Test public void serializeString() {
    String text = "test";
    String actual = serializer.serialize(CIPHER_TEXT, text);
    String expected = text.getClass().getName() + "##0V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void deserializeString() {
    String clazz = "java.lang.String";
    String info = "00V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "#" + clazz + "#" + info + "@" + cipher);

    assertThat(dataInfo.keyClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.valueClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.dataType).isEqualTo(DataType.OBJECT);
    assertThat(dataInfo.cipherText).isEqualTo(cipher);
  }

  @Test public void serializeCustomObject() {
    Foo foo = new Foo();
    String actual = serializer.serialize(CIPHER_TEXT, foo);
    String expected = foo.getClass().getName() + "##0V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void deserializeCustomObject() {
    String clazz = Foo.class.getName();
    String info = "00V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "#" + clazz + "#" + info + "@" + cipher);

    assertThat(dataInfo.keyClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.valueClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.dataType).isEqualTo(DataType.OBJECT);
    assertThat(dataInfo.cipherText).isEqualTo(cipher);
  }

  @Test public void serializeEmptyList() {
    List<String> list = new ArrayList<>();
    String actual = serializer.serialize(CIPHER_TEXT, list);
    String expected = "##1V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void serializeList() {
    List<String> list = new ArrayList<>();
    list.add("test");
    String actual = serializer.serialize(CIPHER_TEXT, list);
    String expected = String.class.getName() + "##1V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void deserializeList() {
    String clazz = "java.lang.String";
    String info = "1V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "##" + info + "@" + cipher);

    assertThat(dataInfo.keyClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.valueClazz).isNull();
    assertThat(dataInfo.dataType).isEqualTo(DataType.LIST);
    assertThat(dataInfo.cipherText).isEqualTo(cipher);
  }

  @Test public void serializeEmptyMap() {
    Map<String, String> map = new HashMap<>();
    String actual = serializer.serialize(CIPHER_TEXT, map);
    String expected = "##2V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void serializeMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    String actual = serializer.serialize(CIPHER_TEXT, map);
    String expected = String.class.getName() + "#" + String.class.getName() + "#2V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void deserializeMap() {
    String clazz = "java.lang.String";
    String info = "2V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "#" + clazz + "#" + info + "@" + cipher);

    assertThat(dataInfo.keyClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.valueClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.dataType).isEqualTo(DataType.MAP);
    assertThat(dataInfo.cipherText).isEqualTo(cipher);
  }

  @Test public void serializeEmptySet() {
    Set<String> set = new HashSet<>();
    String actual = serializer.serialize(CIPHER_TEXT, set);
    String expected = "##3V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void serializeSet() {
    Set<String> set = new HashSet<>();
    set.add("test");
    String actual = serializer.serialize(CIPHER_TEXT, set);
    String expected = String.class.getName() + "##3V@" + CIPHER_TEXT;

    assertThat(actual).isEqualTo(expected);
  }

  @Test public void deserializeSet() {
    String clazz = "java.lang.String";
    String info = "3V";
    String cipher = "cipher";
    DataInfo dataInfo = serializer.deserialize(clazz + "##" + info + "@" + cipher);

    assertThat(dataInfo.keyClazz.getName()).isEqualTo(clazz);
    assertThat(dataInfo.valueClazz).isNull();
    assertThat(dataInfo.dataType).isEqualTo(DataType.SET);
    assertThat(dataInfo.cipherText).isEqualTo(cipher);
  }

}