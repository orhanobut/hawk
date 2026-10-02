package com.orhanobut.hawk


/** Stores type metadata alongside encrypted text. */
interface Serializer {
    fun <T> serialize(cipherText: String?, value: T?): String?
    fun deserialize(plainText: String): DataInfo?
}
