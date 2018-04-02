package com.orhanobut.hawk;

/**
 * Intermediate layer that is used to serialize/deserialize the cipher text
 *
 * <p>Use custom implementation if built-in implementation is not enough.</p>
 *
 * @see HawkSerializer
 */
public interface Serializer {

  /**
   * Serialize the cipher text along with the given data type
   *
   * @return serialized string
   */
  <T> String serialize(String cipherText, T value);

  /**
   * Deserialize the given text according to given DataInfo
   *
   * @return original object
   */
  DataInfo deserialize(String plainText);
}