package com.orhanobut.hawk;

final class DataInfo {

  private final char dataType;
  private final String cipherText;
  private final Class keyClazz;
  private final Class valueClazz;
  private final boolean serializable;
  private final boolean isNewVersion;

  DataInfo(char dataType, boolean serializable, String cipherText, Class keyClazz) {
    this.cipherText = cipherText;
    this.keyClazz = keyClazz;
    this.valueClazz = null;
    this.dataType = dataType;
    this.serializable = serializable;
    this.isNewVersion = false;
  }

  DataInfo(char dataType, String cipherText, Class keyClazz, Class valueClazz) {
    this.cipherText = cipherText;
    this.keyClazz = keyClazz;
    this.valueClazz = valueClazz;
    this.dataType = dataType;
    this.serializable = false;
    this.isNewVersion = true;
  }

  public char getDataType() {
    return dataType;
  }

  String getCipherText() {
    return cipherText;
  }

  Class getKeyClazz() {
    return keyClazz;
  }

  public Class getValueClazz() {
    return valueClazz;
  }

  public boolean isSerializable() {
    return serializable;
  }

  public boolean isNewVersion() {
    return isNewVersion;
  }
}
