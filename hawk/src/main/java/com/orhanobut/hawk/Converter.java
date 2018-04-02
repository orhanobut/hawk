package com.orhanobut.hawk;

/**
 * Used to handle encoding and decoding as an intermediate layer.
 * <p>Implement this interface if a custom implementation is needed</p>
 *
 * @see HawkConverter
 */
public interface Converter {

  /**
   * Encodes the value
   *
   * @param value will be encoded
   * @return the encoded string
   */
  <T> String toString(T value);

  /**
   * Decodes
   *
   * @param value is the encoded data
   * @return the plain value
   * @throws Exception
   */
  <T> T fromString(String value, DataInfo dataInfo) throws Exception;

}
