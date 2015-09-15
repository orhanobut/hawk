package com.orhanobut.hawk;

interface Encoder {

  /**
   * Encodes the value
   *
   * @param value will be encoded
   * @return the encoded string
   */
  <T> byte[] encode(T value);

  /**
   * Decodes
   *
   * @param value is the encoded data
   * @return the plain value
   * @throws Exception
   */
  <T> T decode(byte[] value, DataInfo dataInfo) throws Exception;

}
