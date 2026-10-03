package com.orhanobut.hawk

import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.*
import org.junit.Test

class AesGcmCipherTest {
    private fun newKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test fun roundTripsValuesWithFreshNonces() {
        val secret = newKey()
        for (value in listOf("", "héllo 🦅", "\u0000", "abcd".repeat(25_000))) {
            val first = AesGcmCipher.encrypt("key", value, secret)
            val second = AesGcmCipher.encrypt("key", value, secret)
            assertEquals(value, AesGcmCipher.decrypt("key", first, secret))
            assertFalse(first.copyOfRange(1, 13).contentEquals(second.copyOfRange(1, 13)))
        }
    }

    @Test fun wrongKeysAndModifiedNonceCiphertextOrTagFailAuthentication() {
        val secret = newKey()
        val encrypted = AesGcmCipher.encrypt("key", "value", secret)
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("other", encrypted, secret) }
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("key", encrypted, newKey()) }
        for (offset in listOf(1, 13, encrypted.lastIndex)) {
            val corrupted = encrypted.clone()
            corrupted[offset] = (corrupted[offset].toInt() xor 1).toByte()
            assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("key", corrupted, secret) }
        }
    }

    @Test fun truncatedUnknownVersionAndAppendedDataAreRejected() {
        val secret = newKey()
        for (length in listOf(0, 28)) {
            assertThrows(IllegalArgumentException::class.java) { AesGcmCipher.decrypt("key", ByteArray(length), secret) }
        }
        val encrypted = AesGcmCipher.encrypt("key", "value", secret)
        val unknown = encrypted.clone().apply { this[0] = 2 }
        assertThrows(IllegalArgumentException::class.java) { AesGcmCipher.decrypt("key", unknown, secret) }
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("key", encrypted + byteArrayOf(0), secret) }
    }

}
