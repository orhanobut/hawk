package com.orhanobut.hawk

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Versioned binary envelope shared by JVM and Android providers. */
internal object AesGcmCipher {
    private const val VERSION: Byte = 1
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun encrypt(key: String, value: String, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Let the provider generate the nonce; Android Keystore forbids caller-supplied
        // encryption IVs when randomized encryption is required.
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        check(iv.size == IV_BYTES) { "AES-GCM provider returned an unsupported IV length" }
        cipher.updateAAD(associatedData(key))
        return byteArrayOf(VERSION) + iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    }

    fun decrypt(key: String, envelope: ByteArray, secretKey: SecretKey): String {
        require(envelope.size >= 1 + IV_BYTES + TAG_BITS / 8) { "Truncated AES-GCM value" }
        require(envelope[0] == VERSION) { "Unsupported AES-GCM format version" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_BITS, envelope.copyOfRange(1, 1 + IV_BYTES)))
        cipher.updateAAD(associatedData(key))
        val plaintext = cipher.doFinal(envelope, 1 + IV_BYTES, envelope.size - 1 - IV_BYTES)
        return String(plaintext, Charsets.UTF_8)
    }

    // Bind values to their storage key and the encryption protocol.
    private fun associatedData(key: String): ByteArray =
        "hawk-aes-gcm".toByteArray(Charsets.UTF_8) + byteArrayOf(VERSION) + key.toByteArray(Charsets.UTF_8)
}
