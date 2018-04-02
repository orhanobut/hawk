package com.orhanobut.hawk

import android.content.Context

import org.junit.Before
import org.junit.Test
import org.mockito.InOrder
import org.mockito.Mock

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.mockito.Matchers.anyString
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.initMocks

class DefaultHawkFacadeTest {

  private lateinit var hawkFacade: HawkFacade

  @Mock private lateinit var converter: Converter
  @Mock private lateinit  var encryption: Encryption
  @Mock private lateinit  var serializer: Serializer
  @Mock private lateinit  var storage: Storage
  @Mock private lateinit  var context: Context

  @Before fun setup() {
    initMocks(this)

    val builder = HawkBuilder(context)
        .setConverter(converter)
        .setSerializer(serializer)
        .setEncryption(encryption)
        .setStorage(storage)

    hawkFacade = DefaultHawkFacade(builder)
  }

  //region PUT

  @Test fun putFailsOnNullKey() {
    try {
      hawkFacade.put(null, VALUE)
      fail("Null is not accepted")
    } catch (e: Exception) {
      assertThat(e).hasMessage("Key should not be null")
    }

  }

  @Test fun putSuccess() {
    `when`(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT)
    `when`(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT)
    `when`(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(SERIALIZED_TEXT)
    `when`(storage.put(KEY, SERIALIZED_TEXT)).thenReturn(true)

    assertThat(hawkFacade.put(KEY, VALUE)).isTrue()

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(converter).toString(VALUE)
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT)
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE)
    inOrder.verify(storage).put(KEY, SERIALIZED_TEXT)
  }

  @Test fun putFailsOnConvert() {
    `when`(converter.toString(VALUE)).thenReturn(null)

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse()

    verify(converter).toString(VALUE)
    verifyNoMoreInteractions(encryption, storage, serializer)
  }

  @Test fun putFailsOnEncrypt() {
    `when`(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT)
    `when`(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(null)

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse()

    val inOrder = inOrder(converter, encryption)
    inOrder.verify(converter).toString(VALUE)
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT)
    verifyNoMoreInteractions(serializer, storage)
  }

  @Test fun putFailsOnSerialize() {
    `when`(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT)
    `when`(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT)
    `when`(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(null)

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse()

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(converter).toString(VALUE)
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT)
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE)
    verifyZeroInteractions(storage)
  }

  @Test fun putFailsOnStorage() {
    `when`(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT)
    `when`(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT)
    `when`(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(SERIALIZED_TEXT)
    `when`(storage.put(KEY, SERIALIZED_TEXT)).thenReturn(false)

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse()

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(converter).toString(VALUE)
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT)
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE)
    inOrder.verify(storage).put(KEY, SERIALIZED_TEXT)
  }

  //endregion

  //region GET
  @Test fun getNullKey() {
    assertThat(hawkFacade.get<Any>(null)).isNull()
  }

  @Test fun getWithDefault() {
    `when`(hawkFacade.get<Any>(anyString())).thenReturn(null)

    assertThat(hawkFacade.get("key", "default")).isEqualTo("default")
    assertThat(hawkFacade.get(null, "default")).isEqualTo("default")
  }

  @Test fun getSuccess() {
    `when`(storage.get<Any>(KEY)).thenReturn(SERIALIZED_TEXT)
    `when`(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO)
    `when`(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(CONVERTED_TEXT)
    `when`(converter.fromString<Any>(CONVERTED_TEXT, DATA_INFO)).thenReturn(VALUE)

    assertThat(hawkFacade.get<Any>(KEY)).isEqualTo(VALUE)

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(storage).get<Any>(KEY)
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT)
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT)
    inOrder.verify(converter).fromString<Any>(CONVERTED_TEXT, DATA_INFO)
  }

  @Test fun getFailsOnStorage() {
    `when`(storage.get<Any>(KEY)).thenReturn(null)

    assertThat(hawkFacade.get<Any>(KEY)).isEqualTo(null)

    verify(storage).get<Any>(KEY)
    verifyZeroInteractions(encryption, serializer, converter)
  }

  @Test fun getFailsOnDeserialize() {
    `when`(storage.get<Any>(KEY)).thenReturn(SERIALIZED_TEXT)
    `when`(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(null)

    assertThat(hawkFacade.get<Any>(KEY)).isEqualTo(null)

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(storage).get<Any>(KEY)
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT)

    verifyZeroInteractions(encryption, converter)
  }

  @Test fun getFailsOnDecrypt() {
    `when`(storage.get<Any>(KEY)).thenReturn(SERIALIZED_TEXT)
    `when`(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO)
    `when`(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(null)

    assertThat(hawkFacade.get<Any>(KEY)).isEqualTo(null)

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(storage).get<Any>(KEY)
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT)
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT)

    verifyZeroInteractions(converter)
  }

  @Test fun getFailsOnConvert() {
    `when`(storage.get<Any>(KEY)).thenReturn(SERIALIZED_TEXT)
    `when`(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO)
    `when`(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(CONVERTED_TEXT)
    `when`(converter.fromString<Any>(CONVERTED_TEXT, DATA_INFO)).thenReturn(null)

    assertThat(hawkFacade.get<Any>(KEY)).isEqualTo(null)

    val inOrder = inOrder(converter, encryption, serializer, storage)
    inOrder.verify(storage).get<Any>(KEY)
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT)
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT)
    inOrder.verify(converter).fromString<Any>(CONVERTED_TEXT, DATA_INFO)
  }

  //endregion

  @Test fun count() {
    `when`(storage.count()).thenReturn(100L)

    assertThat(hawkFacade.count()).isEqualTo(100L)
    verifyZeroInteractions(encryption, converter, serializer)
  }

  @Test fun deleteAll() {
    `when`(storage.deleteAll()).thenReturn(true)

    assertThat(hawkFacade.deleteAll()).isTrue()
    verifyZeroInteractions(encryption, converter, serializer)
  }

  @Test fun delete() {
    `when`(storage.delete(KEY)).thenReturn(true)

    assertThat(hawkFacade.delete(KEY)).isTrue()
    verifyZeroInteractions(encryption, converter, serializer)
  }

  @Test fun contains() {
    `when`(storage.contains(KEY)).thenReturn(true)

    assertThat(hawkFacade.contains(KEY)).isTrue()
    verifyZeroInteractions(encryption, converter, serializer)
  }

  @Test fun isBuilt() {
    assertThat(hawkFacade.isBuilt).isTrue()
  }

  companion object {

    private const val KEY = "KEY"
    private const val VALUE = "VALUE"
    private const val CONVERTED_TEXT = "CONVERTED_TEXT"
    private const val CIPHER_TEXT = "CIPHER_TEXT"
    private const val SERIALIZED_TEXT = "SERIALIZED_TEXT"
    private val DATA_INFO = DataInfo(DataInfo.TYPE_OBJECT, CIPHER_TEXT, String::class.java, null)
  }
}