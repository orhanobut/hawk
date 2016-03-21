package com.orhanobut.hawk;

final class DataInfo {

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
