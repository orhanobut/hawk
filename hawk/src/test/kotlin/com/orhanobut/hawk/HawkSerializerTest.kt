package com.orhanobut.hawk

import org.junit.Before
import org.junit.Test

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

import org.junit.Assert.*

class HawkSerializerTest {

  private val CIPHER_TEXT = "CIPHER"
  private lateinit var serializer: Serializer

  @Before fun setup() {
    serializer = HawkSerializer(LogInterceptor {
      // ignore
    })
  }

  internal class Foo

  @Test fun serializeString() {
    val text = "test"
    val actual = serializer.serialize(CIPHER_TEXT, text)
    val expected = text.javaClass.name + "##0V@" + CIPHER_TEXT

    assertEquals(expected, actual)
  }

  @Test fun deserializeString() {
    val clazz = "java.lang.String"
    val info = "00V"
    val cipher = "cipher"
    val dataInfo = requireNotNull(serializer.deserialize("$clazz#$clazz#$info@$cipher"))

    assertEquals(clazz, dataInfo.keyClazz!!.name)
    assertEquals(clazz, dataInfo.valueClazz!!.name)
    assertEquals(DataInfo.TYPE_OBJECT, dataInfo.dataType)
    assertEquals(cipher, dataInfo.cipherText)
  }

  @Test fun serializeCustomObject() {
    val foo = Foo()
    val actual = serializer.serialize(CIPHER_TEXT, foo)
    val expected = foo.javaClass.name + "##0V@" + CIPHER_TEXT

    assertEquals(expected, actual)
  }

  @Test fun deserializeCustomObject() {
    val clazz = Foo::class.java.name
    val info = "00V"
    val cipher = "cipher"
    val dataInfo = requireNotNull(serializer.deserialize("$clazz#$clazz#$info@$cipher"))

    assertEquals(clazz, dataInfo.keyClazz!!.name)
    assertEquals(clazz, dataInfo.valueClazz!!.name)
    assertEquals(DataInfo.TYPE_OBJECT, dataInfo.dataType)
    assertEquals(cipher, dataInfo.cipherText)
  }

  @Test fun serializeEmptyList() {
    val list = ArrayList<String>()
    val actual = serializer.serialize<List<String>>(CIPHER_TEXT, list)
    val expected = "##1V@$CIPHER_TEXT"

    assertEquals(expected, actual)
  }

  @Test fun serializeList() {
    val list = ArrayList<String>()
    list.add("test")
    val actual = serializer.serialize<List<String>>(CIPHER_TEXT, list)
    val expected = String::class.java.name + "##1V@" + CIPHER_TEXT

    assertEquals(expected, actual)
  }

  @Test fun deserializeList() {
    val clazz = "java.lang.String"
    val info = "1V"
    val cipher = "cipher"
    val dataInfo = requireNotNull(serializer.deserialize("$clazz##$info@$cipher"))

    assertEquals(clazz, dataInfo.keyClazz!!.name)
    assertNull(dataInfo.valueClazz)
    assertEquals(DataInfo.TYPE_LIST, dataInfo.dataType)
    assertEquals(cipher, dataInfo.cipherText)
  }

  @Test fun serializeEmptyMap() {
    val map = HashMap<String, String>()
    val actual = serializer.serialize<Map<String, String>>(CIPHER_TEXT, map)
    val expected = "##2V@$CIPHER_TEXT"

    assertEquals(expected, actual)
  }

  @Test fun serializeMap() {
    val map = HashMap<String, String>()
    map["key"] = "value"
    val actual = serializer.serialize<Map<String, String>>(CIPHER_TEXT, map)
    val expected = String::class.java.name + "#" + String::class.java.name + "#2V@" + CIPHER_TEXT

    assertEquals(expected, actual)
  }

  @Test fun deserializeMap() {
    val clazz = "java.lang.String"
    val info = "2V"
    val cipher = "cipher"
    val dataInfo = requireNotNull(serializer.deserialize("$clazz#$clazz#$info@$cipher"))

    assertEquals(clazz, dataInfo.keyClazz!!.name)
    assertEquals(clazz, dataInfo.valueClazz!!.name)
    assertEquals(DataInfo.TYPE_MAP, dataInfo.dataType)
    assertEquals(cipher, dataInfo.cipherText)
  }

  @Test fun serializeEmptySet() {
    val set = HashSet<String>()
    val actual = serializer.serialize<Set<String>>(CIPHER_TEXT, set)
    val expected = "##3V@$CIPHER_TEXT"

    assertEquals(expected, actual)
  }

  @Test fun serializeSet() {
    val set = HashSet<String>()
    set.add("test")
    val actual = serializer.serialize<Set<String>>(CIPHER_TEXT, set)
    val expected = String::class.java.name + "##3V@" + CIPHER_TEXT

    assertEquals(expected, actual)
  }

  @Test fun deserializeSet() {
    val clazz = "java.lang.String"
    val info = "3V"
    val cipher = "cipher"
    val dataInfo = requireNotNull(serializer.deserialize("$clazz##$info@$cipher"))

    assertEquals(clazz, dataInfo.keyClazz!!.name)
    assertNull(dataInfo.valueClazz)
    assertEquals(DataInfo.TYPE_SET, dataInfo.dataType)
    assertEquals(cipher, dataInfo.cipherText)
  }
}
