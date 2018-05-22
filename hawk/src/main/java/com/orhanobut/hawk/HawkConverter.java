package com.orhanobut.hawk;

import com.google.gson.reflect.TypeToken;

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
final class HawkConverter implements Converter {

  private final Parser parser;

  public HawkConverter(Parser parser) {
    if (parser == null) {
      throw new NullPointerException("Parser should not be null");
    }
    this.parser = parser;
  }

  @Override public <T> String toString(T value) {
    if (value == null) {
      return null;
    }
    return parser.toJson(value);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T fromString(String value, DataInfo info) throws Exception {
    if (value == null) {
      return null;
    }
    HawkUtils.checkNull("data info", info);

    Class<?> keyType = info.keyClazz;
    Class<?> valueType = info.valueClazz;

    switch (info.dataType) {
      case DataInfo.TYPE_OBJECT:
        return toObject(value, keyType);
      case DataInfo.TYPE_LIST:
        return toList(value, keyType);
      case DataInfo.TYPE_MAP:
        return toMap(value, keyType, valueType);
      case DataInfo.TYPE_SET:
        return toSet(value, keyType);
      default:
        return null;
    }
  }

  private <T> T toObject(String json, Class<?> type) throws Exception {
    return parser.fromJson(json, type);
  }

  @SuppressWarnings("unchecked")
  private <T> T toList(String json, Class<?> type) throws Exception {
    if (type == null) {
      return (T) new ArrayList<>();
    }
    List<T> list = parser.fromJson(
        json,
        TypeToken.getParameterized(List.class, type).getType()
    );
    return (T) list;
  }

  @SuppressWarnings("unchecked")
  private <T> T toSet(String json, Class<?> type) throws Exception {
    Set<T> resultSet = new HashSet<>();
    if (type == null) {
      return (T) resultSet;
    }
    Set<T> set = parser.fromJson(json, TypeToken.getParameterized(Set.class, type).getType());
    return (T) set;
  }

  @SuppressWarnings("unchecked")
  private <K, V, T> T toMap(String json, Class<?> keyType, Class<?> valueType) throws Exception {
    Map<K, V> resultMap = new HashMap<>();
    if (keyType == null || valueType == null) {
      return (T) resultMap;
    }
    Map<K, V> map = parser.fromJson(json, TypeToken.getParameterized(Map.class, keyType, valueType).getType());
    return (T) map;
  }

}
