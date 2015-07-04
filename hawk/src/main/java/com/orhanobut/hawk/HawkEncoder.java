package com.orhanobut.hawk;

import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of encoding and decoding.
 * List types will be encoded/decoded by parser
 * Serializable types will be encoded/decoded object stream
 * Not serializable objects will be encoded/decoded by parser
 *
 * @author Orhan Obut
 */
final class HawkEncoder implements Encoder {

  private final Parser parser;

  public HawkEncoder(Parser parser) {
    if (parser == null) {
      throw new NullPointerException("Parser may not be null");
    }
    this.parser = parser;
  }

  @Override
  public <T> byte[] encode(T value) {
    if (value == null) {
      return null;
    }
    byte[] bytes;
    String json = parser.toJson(value);
    bytes = json.getBytes();

    return bytes;
  }

  @Override
  @Deprecated
  public <T> byte[] encode(List<T> value) {
    if (value == null) {
      return null;
    }
    String json = parser.toJson(value);
    return json.getBytes();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T decode(byte[] bytes, DataInfo info) throws Exception {
    if (bytes == null) {
      return null;
    }

    if (!info.isNewVersion()) {
      return decodeOld(bytes, info);
    } else {
      return decodeNew(bytes, info);
    }
  }

  private <T> T decodeOld(byte[] bytes, DataInfo info) throws Exception {
    boolean isList = info.getDataType() == DataType.LIST;

    // if the value is not list and serializable, then use the normal deserialize
    if (!isList && info.isSerializable()) {
      return toSerializable(bytes);
    }

    // convert to the string json
    String json = new String(bytes);

    Class<?> type = info.getKeyClazz();
    if (!isList) {
      return parser.fromJson(json, type);
    }

    return toList(json, type);
  }

  private <T> T decodeNew(byte[] bytes, DataInfo info) throws Exception {
    // convert to the string json
    String json = new String(bytes);

    Class<?> keyType = info.getKeyClazz();
    Class<?> valueType = info.getValueClazz();

    switch (info.getDataType()) {
      case OBJECT:
        return toObject(json, keyType);
      case LIST:
        return toList(json, keyType);
      case MAP:
        return toMap(json, keyType, valueType);
      case SET:
        return toSet(json, keyType);
    }
    return null;
  }

  @Override
  @Deprecated
  public <T> T decodeSerializable(String value) throws Exception {
    return toSerializable(value.getBytes());
  }

  private <T> T toObject(String json, Class<?> type) throws Exception {
    return parser.fromJson(json, type);
  }

  @SuppressWarnings("unchecked")
  private <T> T toList(String json, Class<?> type) throws Exception {
    if (type == null) {
      return (T) new ArrayList<>();
    }
    List<T> list;
    list = parser.fromJson(json, new TypeToken<List<T>>() {
    }.getType());

    int size = list.size();
    for (int i = 0; i < size; i++) {
      list.set(i, (T) parser.fromJson(parser.toJson(list.get(i)), type));
    }
    return (T) list;
  }

  @SuppressWarnings("unchecked")
  private <T> T toSet(String json, Class<?> type) throws Exception {
    if (type == null) {
      return (T) new HashSet<>();
    }
    Set<T> set;
    set = parser.fromJson(json, new TypeToken<Set<T>>() {
    }.getType());

    Set<T> resultSet = new HashSet<>();
    for (T t : set) {
      String valueJson = parser.toJson(t);
      T value = parser.fromJson(valueJson, type);
      resultSet.add(value);
    }
    return (T) resultSet;
  }

  @SuppressWarnings("unchecked")
  private <K, V, T> T toMap(String json, Class<?> keyType, Class<?> valueType) throws Exception {
    if (keyType == null || valueType == null) {
      return (T) new HashMap<>();
    }
    Map<K, V> map;
    map = parser.fromJson(json, new TypeToken<Map<K, V>>() {
    }.getType());

    Map<K, V> resultMap = new HashMap<>();
    for (Map.Entry<K, V> entry : map.entrySet()) {
      String keyJson = parser.toJson(entry.getKey());
      K k = parser.fromJson(keyJson, keyType);

      String valueJson = parser.toJson(entry.getValue());
      V v = parser.fromJson(valueJson, valueType);
      resultMap.put(k, v);
    }
    return (T) resultMap;
  }

  /**
   * Converts byte[] to a serializable object
   *
   * @param bytes the data
   * @param <T>   object type
   * @return the serializable object
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  private <T> T toSerializable(byte[] bytes) {
    ObjectInputStream inputStream = null;
    try {
      inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
    } catch (IOException e) {
      Logger.d(e.getMessage());
    }

    if (inputStream == null) {
      return null;
    }

    T result = null;
    try {
      result = (T) inputStream.readObject();
    } catch (ClassNotFoundException | IOException e) {
      Logger.d(e.getMessage());
    }
    return result;
  }

}
