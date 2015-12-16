package com.orhanobut.hawk;

import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DataHelper {

  private static final char DELIMITER = '@';
  private static final char INFO_DELIMITER = '#';
  private static final char NEW_VERSION = 'V';

  @Deprecated private static final char FLAG_SERIALIZABLE = '1';

  static final char DATATYPE_OBJECT = '0';
  static final char DATATYPE_LIST = '1';
  static final char DATATYPE_MAP = '2';
  static final char DATATYPE_SET = '3';

  private DataHelper() {
    // no instance
  }

  /**
   * Saved data contains more info than cipher text, this method will demarshall the data
   *
   * @param storedText is the saved data
   * @return the DataInfo object which contains all necessary information
   */
  static DataInfo getDataInfo(String storedText) {
    if (TextUtils.isEmpty(storedText)) {
      throw new NullPointerException("Text should not be null or empty");
    }

    int index = storedText.indexOf(INFO_DELIMITER, 0);
    if (index == -1) {
      return getOldDataInfo(storedText);
    }
    return getNewDataInfo(storedText, index);
  }

  static DataInfo getNewDataInfo(String storedText, int indexFirstDelimiter) {
    String keyClass = storedText.substring(0, indexFirstDelimiter);

    int start = indexFirstDelimiter + 1;
    int index = storedText.indexOf(INFO_DELIMITER, start);

    if (index == -1) {
      throw new IllegalArgumentException("Text should contain delimiter");
    }

    String valueClass = storedText.substring(start, index);

    char dataType = storedText.charAt(++index);

    //skip NEW_VERSION + DELIMITER
    index += 3;

    String cipherText = storedText.substring(index);

    if (TextUtils.isEmpty(cipherText)) {
      throw new IllegalArgumentException("Invalid stored cipher text");
    }

    Class<?> keyClazz = null;
    Class<?> valueClazz = null;

    if (!TextUtils.isEmpty(keyClass)) {
      try {
        keyClazz = Class.forName(keyClass);
      } catch (ClassNotFoundException e) {
        Logger.d(e.getMessage());
      }
    }

    if (!TextUtils.isEmpty(valueClass)) {
      try {
        valueClazz = Class.forName(valueClass);
      } catch (ClassNotFoundException e) {
        Logger.d(e.getMessage());
      }
    }
    return new DataInfo(dataType, cipherText, keyClazz, valueClazz);
  }

  @Deprecated static DataInfo getOldDataInfo(String storedText) {
    int index = storedText.indexOf(DELIMITER);

    if (index == -1) {
      throw new IllegalArgumentException("Text should contain delimiter");
    }

    String text = storedText.substring(0, index);
    String cipherText = storedText.substring(index + 1);

    if (TextUtils.isEmpty(text) || TextUtils.isEmpty(cipherText)) {
      throw new IllegalArgumentException("Invalid stored text");
    }

    boolean serializable = text.charAt(text.length() - 1) == FLAG_SERIALIZABLE;
    char dataType = text.charAt(text.length() - 2);

    String className = text.substring(0, text.length() - 2);

    Class<?> clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      Logger.d(e.getMessage());
    }

    return new DataInfo(dataType, serializable, cipherText, clazz);
  }

  static <T> String addType(String cipherText, T t) {
    if (TextUtils.isEmpty(cipherText)) {
      throw new NullPointerException("Cipher text should not be null or empty");
    }
    if (t == null) {
      throw new NullPointerException("Value should not be null");
    }

    if (List.class.isAssignableFrom(t.getClass())) {
      return addListType(cipherText, (List) t);
    }

    if (Map.class.isAssignableFrom(t.getClass())) {
      return addMapType(cipherText, (Map) t);
    }

    if (Set.class.isAssignableFrom(t.getClass())) {
      return addSetType(cipherText, (Set) t);
    }

    String keyClassName = t.getClass().getName();
    return createType(keyClassName, "", DATATYPE_OBJECT, cipherText);
  }

  private static String addListType(String cipherText, List list) {
    String keyClassName = "";
    if (!list.isEmpty()) {
      keyClassName = list.get(0).getClass().getName();
    }
    return createType(keyClassName, "", DATATYPE_LIST, cipherText);
  }

  private static String addMapType(String cipherText, Map<?, ?> map) {
    String keyClassName = "";
    String valueClassName = "";
    if (!map.isEmpty()) {
      Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
      Map.Entry<?, ?> entry = iterator.next();
      keyClassName = entry.getKey().getClass().getName();
      valueClassName = entry.getValue().getClass().getName();
    }
    return createType(keyClassName, valueClassName, DATATYPE_MAP, cipherText);
  }

  private static String addSetType(String cipherText, Set set) {
    String keyClassName = "";
    if (!set.isEmpty()) {
      Iterator<?> iterator = set.iterator();
      keyClassName = iterator.next().getClass().getName();
    }
    return createType(keyClassName, "", DATATYPE_SET, cipherText);
  }


  private static String createType(String keyClassName, String valueClassName, char dataType, String cipherText) {
    return keyClassName + INFO_DELIMITER +
        valueClassName + INFO_DELIMITER +
        dataType + NEW_VERSION + DELIMITER +
        cipherText;
  }

  static String encodeBase64(byte[] bytes) {
    try {
      return Base64.encodeToString(bytes, Base64.DEFAULT);
    } catch (Exception e) {
      Logger.w(e.getMessage());
      return null;
    }
  }

  static byte[] decodeBase64(String value) {
    try {
      return Base64.decode(value, Base64.DEFAULT);
    } catch (Exception e) {
      Logger.w(e.getMessage());
      return null;
    }
  }

}
