package com.orhanobut.hawk;

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
 */
final class HawkEncoder implements Encoder {

  private final Parser parser;

  public HawkEncoder(Parser parser) {
    if (parser == null) {
      throw new NullPointerException("Parser should not be null");
    }
    this.parser = parser;
  }

  @Override public <T> byte[] encode(T value) {
    if (value == null) {
      return null;
    }
    byte[] bytes;
    String json = parser.toJson(value);
    bytes = json.getBytes();

    return bytes;
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T decode(byte[] bytes, DataInfo info) throws Exception {
    if (bytes == null) {
      return null;
    }
    HawkUtils.checkNull("data info", info);

    // convert to the string json
    String json = new String(bytes);

    Class<?> keyType = info.keyClazz;
    Class<?> valueType = info.valueClazz;

    switch (info.dataType) {
    case OBJECT:
      return toObject(json, keyType);
    case LIST:
      return toList(json, keyType);
    case MAP:
      return toMap(json, keyType, valueType);
    case SET:
      return toSet(json, keyType);
    default:
      return null;
    }
  }

  private <T> T toObject(String json, Class<?> type) throws Exception {
    return parser.fromJson(json, type);
  }

  @SuppressWarnings("unchecked") private <T> T toList(String json, Class<?> type) throws Exception {
    if (type == null) {
      return (T) new ArrayList<>();
    }
    List<T> list = parser.fromJson(json, List.class);

    int size = list.size();
    for (int i = 0; i < size; i++) {
      list.set(i, (T) parser.fromJson(parser.toJson(list.get(i)), type));
    }
    return (T) list;
  }

  @SuppressWarnings("unchecked") private <T> T toSet(String json, Class<?> type) throws Exception {
    Set<T> resultSet = new HashSet<>();
    if (type == null) {
      return (T) resultSet;
    }
    Set<T> set = parser.fromJson(json, Set.class);

    for (T t : set) {
      String valueJson = parser.toJson(t);
      T value = parser.fromJson(valueJson, type);
      resultSet.add(value);
    }
    return (T) resultSet;
  }

  @SuppressWarnings("unchecked") private <K, V, T> T toMap(String json, Class<?> keyType, Class<?> valueType) throws Exception {
    Map<K, V> resultMap = new HashMap<>();
    if (keyType == null || valueType == null) {
      return (T) resultMap;
    }
    Map<K, V> map = parser.fromJson(json, Map.class);

    for (Map.Entry<K, V> entry : map.entrySet()) {
      String keyJson = parser.toJson(entry.getKey());
      K k = parser.fromJson(keyJson, keyType);

      String valueJson = parser.toJson(entry.getValue());
      V v = parser.fromJson(valueJson, valueType);
      resultMap.put(k, v);
    }
    return (T) resultMap;
  }

}
