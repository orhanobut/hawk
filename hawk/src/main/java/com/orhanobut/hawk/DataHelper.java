package com.orhanobut.hawk;

import android.text.TextUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DataHelper {

  private static final char DELIMITER = '@';
  private static final char INFO_DELIMITER = '#';
  private static final char NEW_VERSION = 'V';

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
    HawkUtils.checkNullOrEmpty("Text", storedText);

    int index = storedText.indexOf(DELIMITER);
    if (index == -1) {
      throw new IllegalArgumentException("storedText is not valid");
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
    HawkUtils.checkNullOrEmpty("Cipher text", cipherText);

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

  //TODO optimize
  static <T> String addType(String cipherText, T t) {
    HawkUtils.checkNullOrEmpty("Cipher text", cipherText);
    HawkUtils.checkNull("Value", t);

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

}
