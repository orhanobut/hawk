package com.orhanobut.hawk;

interface Converter {

  /**
   * Encodes the value
   *
   * @param value will be encoded
   *
   * @return the encoded string
   */
  <T> String toString(T value);

  /**
   * Decodes
   *
   * @param value is the encoded data
   *
   * @return the plain value
   *
   * @throws Exception
   */
  <T> T decode(String value, DataInfo dataInfo) throws Exception;

}
