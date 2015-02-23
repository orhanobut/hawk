package com.orhanobut.hawk;

import java.util.List;

/**
 * @author Orhan Obut
 */
interface Encoder {

    /**
     * Encodes the value and encrypts it
     *
     * @param value will be encoded
     * @return the encoded string
     */
    <T> String encode(T value);

    /**
     * Encodes the list value and encrypts it
     *
     * @param value will be encoded
     * @return the encoded string
     */
    <T> String encode(List<T> value);

    /**
     * Decodes and decrypts the cipher text
     *
     * @param value is the encoded data
     * @return the plain value
     * @throws Exception
     */
    <T> T decode(String value) throws Exception;

}
