package com.orhanobut.hawk;

final class DataInfo {

  final DataType dataType;
  final String cipherText;
  final Class keyClazz;
  final Class valueClazz;

  DataInfo(DataType dataType, String cipherText, Class keyClazz, Class valueClazz) {
    this.cipherText = cipherText;
    this.keyClazz = keyClazz;
    this.valueClazz = valueClazz;
    this.dataType = dataType;
  }
}
