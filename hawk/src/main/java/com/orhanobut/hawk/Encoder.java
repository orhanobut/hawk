package com.orhanobut.hawk;

import java.util.List;

/**
 * @author Orhan Obut
 */
interface Encoder {

  /**
   * Encodes the value
   *
   * @param value will be encoded
   * @return the encoded string
   */
  <T> byte[] encode(T value);

  /**
   * Encodes the list value
   *
   * @param value will be encoded
   * @return the encoded string
   */
  <T> byte[] encode(List<T> value);

  /**
   * Decodes
   *
   * @param value is the encoded data
   * @return the plain value
   * @throws Exception
   */
  <T> T decode(byte[] value, DataInfo dataInfo) throws Exception;

  /**
   * Decodes the text
   *
   * @param value is the encoded data
   * @return the plain value
   * @throws Exception
   */
  <T> T decodeSerializable(String value) throws Exception;

}
