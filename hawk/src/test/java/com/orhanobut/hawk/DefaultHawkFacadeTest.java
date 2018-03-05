package com.orhanobut.hawk;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DefaultHawkFacadeTest {

  private static final String KEY = "KEY";
  private static final String VALUE = "VALUE";
  private static final String CONVERTED_TEXT = "CONVERTED_TEXT";
  private static final String CIPHER_TEXT = "CIPHER_TEXT";
  private static final String SERIALIZED_TEXT = "SERIALIZED_TEXT";
  private static final DataInfo DATA_INFO = new DataInfo(DataInfo.TYPE_OBJECT, CIPHER_TEXT, String.class, null);

  private HawkFacade hawkFacade;

  @Mock Converter converter;
  @Mock Encryption encryption;
  @Mock Serializer serializer;
  @Mock Storage storage;
  @Mock Context context;

  @Before public void setup() {
    initMocks(this);

    HawkBuilder builder = new HawkBuilder(context)
        .setConverter(converter)
        .setSerializer(serializer)
        .setEncryption(encryption)
        .setStorage(storage);

    hawkFacade = new DefaultHawkFacade(builder);
  }

  //region PUT

  @Test public void putFailsOnNullKey() {
    try {
      hawkFacade.put(null, VALUE);
      fail("Null is not accepted");
    } catch (Exception e) {
      assertThat(e).hasMessage("Key should not be null");
    }
  }

  @Test public void putSuccess() throws Exception {
    when(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT);
    when(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT);
    when(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(SERIALIZED_TEXT);
    when(storage.put(KEY, SERIALIZED_TEXT)).thenReturn(true);

    assertThat(hawkFacade.put(KEY, VALUE)).isTrue();

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(converter).toString(VALUE);
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT);
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE);
    inOrder.verify(storage).put(KEY, SERIALIZED_TEXT);
  }

  @Test public void putFailsOnConvert() throws Exception {
    when(converter.toString(VALUE)).thenReturn(null);

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse();

    verify(converter).toString(VALUE);
    verifyNoMoreInteractions(encryption, storage, serializer);
  }

  @Test public void putFailsOnEncrypt() throws Exception {
    when(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT);
    when(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(null);

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse();

    InOrder inOrder = inOrder(converter, encryption);
    inOrder.verify(converter).toString(VALUE);
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT);
    verifyNoMoreInteractions(serializer, storage);
  }

  @Test public void putFailsOnSerialize() throws Exception {
    when(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT);
    when(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT);
    when(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(null);

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse();

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(converter).toString(VALUE);
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT);
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE);
    verifyZeroInteractions(storage);
  }

  @Test public void putFailsOnStorage() throws Exception {
    when(converter.toString(VALUE)).thenReturn(CONVERTED_TEXT);
    when(encryption.encrypt(KEY, CONVERTED_TEXT)).thenReturn(CIPHER_TEXT);
    when(serializer.serialize(CIPHER_TEXT, VALUE)).thenReturn(SERIALIZED_TEXT);
    when(storage.put(KEY, SERIALIZED_TEXT)).thenReturn(false);

    assertThat(hawkFacade.put(KEY, VALUE)).isFalse();

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(converter).toString(VALUE);
    inOrder.verify(encryption).encrypt(KEY, CONVERTED_TEXT);
    inOrder.verify(serializer).serialize(CIPHER_TEXT, VALUE);
    inOrder.verify(storage).put(KEY, SERIALIZED_TEXT);
  }

  //endregion

  //region GET
  @Test public void getNullKey() {
    assertThat(hawkFacade.get(null)).isNull();
  }

  @Test public void getWithDefault() {
    when(hawkFacade.get(anyString())).thenReturn(null);

    assertThat(hawkFacade.get("key", "default")).isEqualTo("default");
    assertThat(hawkFacade.get(null, "default")).isEqualTo("default");
  }

  @Test public void getSuccess() throws Exception {
    when(storage.get(KEY)).thenReturn(SERIALIZED_TEXT);
    when(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO);
    when(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(CONVERTED_TEXT);
    when(converter.fromString(CONVERTED_TEXT, DATA_INFO)).thenReturn(VALUE);

    assertThat(hawkFacade.get(KEY)).isEqualTo(VALUE);

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(storage).get(KEY);
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT);
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT);
    inOrder.verify(converter).fromString(CONVERTED_TEXT, DATA_INFO);
  }

  @Test public void getFailsOnStorage() throws Exception {
    when(storage.get(KEY)).thenReturn(null);

    assertThat(hawkFacade.get(KEY)).isEqualTo(null);

    verify(storage).get(KEY);
    verifyZeroInteractions(encryption, serializer, converter);
  }

  @Test public void getFailsOnDeserialize() throws Exception {
    when(storage.get(KEY)).thenReturn(SERIALIZED_TEXT);
    when(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(null);

    assertThat(hawkFacade.get(KEY)).isEqualTo(null);

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(storage).get(KEY);
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT);

    verifyZeroInteractions(encryption, converter);
  }

  @Test public void getFailsOnDecrypt() throws Exception {
    when(storage.get(KEY)).thenReturn(SERIALIZED_TEXT);
    when(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO);
    when(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(null);

    assertThat(hawkFacade.get(KEY)).isEqualTo(null);

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(storage).get(KEY);
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT);
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT);

    verifyZeroInteractions(converter);
  }

  @Test public void getFailsOnConvert() throws Exception {
    when(storage.get(KEY)).thenReturn(SERIALIZED_TEXT);
    when(serializer.deserialize(SERIALIZED_TEXT)).thenReturn(DATA_INFO);
    when(encryption.decrypt(KEY, CIPHER_TEXT)).thenReturn(CONVERTED_TEXT);
    when(converter.fromString(CONVERTED_TEXT, DATA_INFO)).thenReturn(null);

    assertThat(hawkFacade.get(KEY)).isEqualTo(null);

    InOrder inOrder = inOrder(converter, encryption, serializer, storage);
    inOrder.verify(storage).get(KEY);
    inOrder.verify(serializer).deserialize(SERIALIZED_TEXT);
    inOrder.verify(encryption).decrypt(KEY, CIPHER_TEXT);
    inOrder.verify(converter).fromString(CONVERTED_TEXT, DATA_INFO);
  }

  //endregion

  @Test public void count() {
    when(storage.count()).thenReturn(100L);

    assertThat(hawkFacade.count()).isEqualTo(100L);
    verifyZeroInteractions(encryption, converter, serializer);
  }

  @Test public void deleteAll() {
    when(storage.deleteAll()).thenReturn(true);

    assertThat(hawkFacade.deleteAll()).isTrue();
    verifyZeroInteractions(encryption, converter, serializer);
  }

  @Test public void delete() {
    when(storage.delete(KEY)).thenReturn(true);

    assertThat(hawkFacade.delete(KEY)).isTrue();
    verifyZeroInteractions(encryption, converter, serializer);
  }

  @Test public void contains() {
    when(storage.contains(KEY)).thenReturn(true);

    assertThat(hawkFacade.contains(KEY)).isTrue();
    verifyZeroInteractions(encryption, converter, serializer);
  }

  @Test public void isBuilt() {
    assertThat(hawkFacade.isBuilt()).isTrue();
  }
}