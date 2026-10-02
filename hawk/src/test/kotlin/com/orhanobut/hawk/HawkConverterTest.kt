package com.orhanobut.hawk

import com.google.gson.Gson

import org.junit.Before
import org.junit.Test

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

import org.junit.Assert.*



class HawkConverterTest {

  private lateinit var converter: Converter
  private lateinit var parser: Parser
  private lateinit var serializer: Serializer

  internal class Foo

  @Before fun setup() {
    parser = GsonParser(Gson())
    converter = HawkConverter(parser)
    serializer = HawkSerializer(LogInterceptor {
      // ignore
    })
  }

  @Test fun createInstanceWithInvalidValues() {
    try {
      HawkConverter(null)
      fail()
    } catch (e: Exception) {
      assertEquals("Parser should not be null", e.message)
    }

  }

  @Test fun encodeInvalidValues() {
    assertNull(converter.toString<Any>(null))
  }

  @Test fun encodeString() {
    val text = "text"
    val expected = parser.toJson(text)
    val actual = converter.toString(text)

    assertEquals(expected, actual)
  }

  @Test fun encodeCustomObject() {
    val data = Foo()
    val expected = parser.toJson(data)
    val actual = converter.toString(data)

    assertEquals(expected, actual)
  }

  @Test fun encodeList() {
    val data = ArrayList<String>()
    data.add("test")
    val expected = parser.toJson(data)
    val actual = converter.toString<List<String>>(data)

    assertEquals(expected, actual)
  }

  @Test fun encodeMap() {
    val data = HashMap<String, String>()
    data["key"] = "value"
    val expected = parser.toJson(data)
    val actual = converter.toString<Map<String, String>>(data)

    assertEquals(expected, actual)
  }

  @Test fun encodeSet() {
    val data = HashSet<String>()
    data.add("key")
    val expected = parser.toJson(data)
    val actual = converter.toString<Set<String>>(data)

    assertEquals(expected, actual)
  }

  @Test @Throws(Exception::class)
  fun decodeInvalidValues() {
    assertNull(converter.fromString<Any>(null, null))
    try {
      assertNull(converter.fromString<Any>("value", null))
      fail()
    } catch (e: Exception) {
      assertEquals("data info should not be null", e.message)
    }

  }

  @Test @Throws(Exception::class)
  fun decodeObject() {
    val clazz = "java.lang.String"
    val info = "00V"
    val cipher = "cipher"
    val dataInfo = serializer.deserialize("$clazz##$info@$cipher")
    val actual = converter.fromString<String>("\"$cipher\"", dataInfo)
    assertEquals(cipher, actual)
  }

}
