package com.orhanobut.hawk;

@SuppressWarnings("WeakerAccess")
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