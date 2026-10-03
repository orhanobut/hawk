package com.orhanobut.hawk


import android.util.Base64

/** Base64 encoding only: this does not encrypt or protect stored values. */
open class NoEncryption : Encryption {
    override fun init(): Boolean = true
    @Throws(Exception::class)
    override fun encrypt(key: String, value: String): String = encodeBase64(value.toByteArray())
    @Throws(Exception::class)
    override fun decrypt(key: String, value: String): String = String(decodeBase64(value), Charsets.UTF_8)
    internal fun encodeBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.DEFAULT)
    internal fun decodeBase64(value: String): ByteArray = Base64.decode(value, Base64.DEFAULT)
}
