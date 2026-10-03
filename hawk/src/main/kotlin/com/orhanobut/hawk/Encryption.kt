package com.orhanobut.hawk


/** Encryption layer. A null result represents failure. */
interface Encryption {
    fun init(): Boolean
    @Throws(Exception::class)
    fun encrypt(key: String, value: String): String?
    @Throws(Exception::class)
    fun decrypt(key: String, value: String): String?
}
