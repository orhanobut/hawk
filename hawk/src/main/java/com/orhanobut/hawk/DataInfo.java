package com.orhanobut.hawk;

final class DataInfo {

  static final char TYPE_OBJECT = '0';
  static final char TYPE_LIST = '1';
  static final char TYPE_MAP = '2';
  static final char TYPE_SET = '3';

  final char dataType;
  final String cipherText;
  final Class keyClazz;
  final Class valueClazz;

  DataInfo(char dataType, String cipherText, Class keyClazz, Class valueClazz) {
    this.cipherText = cipherText;
    this.keyClazz = keyClazz;
    this.valueClazz = valueClazz;
    this.dataType = dataType;
  }
}
