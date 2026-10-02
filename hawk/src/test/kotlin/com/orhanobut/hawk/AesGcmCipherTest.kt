package com.orhanobut.hawk

import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.*
import org.junit.Test

class AesGcmCipherTest {
    private fun newKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test fun roundTripsEmptyUnicodeAndLargeValues() {
        val secret = newKey()
        for (value in listOf("", "héllo 🦅", "\u0000", "abcd".repeat(25_000))) {
            val encrypted = AesGcmCipher.encrypt("key", value, secret)
            assertEquals(value, AesGcmCipher.decrypt("key", encrypted, secret))
        }
    }

    @Test fun repeatedEncryptionUsesDifferentNonces() {
        val secret = newKey()
        val first = AesGcmCipher.encrypt("key", "value", secret)
        val second = AesGcmCipher.encrypt("key", "value", secret)
        assertFalse(first.contentEquals(second))
        assertFalse(first.copyOfRange(1, 13).contentEquals(second.copyOfRange(1, 13)))
        assertEquals("value", AesGcmCipher.decrypt("key", first, secret))
        assertEquals("value", AesGcmCipher.decrypt("key", second, secret))
    }

    @Test fun wrongStorageKeyAndEncryptionKeyFailAuthentication() {
        val secret = newKey()
        val encrypted = AesGcmCipher.encrypt("first", "value", secret)
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("second", encrypted, secret) }
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("first", encrypted, newKey()) }
    }

    @Test fun modifiedNonceCiphertextAndTagAreRejected() {
        val secret = newKey()
        val encrypted = AesGcmCipher.encrypt("key", "value", secret)
        for (offset in listOf(1, 13, encrypted.lastIndex)) {
            val corrupted = encrypted.clone()
            corrupted[offset] = (corrupted[offset].toInt() xor 1).toByte()
            assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("key", corrupted, secret) }
        }
    }

    @Test fun truncatedUnknownVersionAndAppendedDataAreRejected() {
        val secret = newKey()
        for (length in 0 until 29) {
            assertThrows(IllegalArgumentException::class.java) { AesGcmCipher.decrypt("key", ByteArray(length), secret) }
        }
        val encrypted = AesGcmCipher.encrypt("key", "value", secret)
        val unknown = encrypted.clone().apply { this[0] = 2 }
        assertThrows(IllegalArgumentException::class.java) { AesGcmCipher.decrypt("key", unknown, secret) }
        assertThrows(AEADBadTagException::class.java) { AesGcmCipher.decrypt("key", encrypted + byteArrayOf(0), secret) }
    }

    @Test fun unavailableKeystoreDoesNotFallBackToEncoding() {
        val unavailable = object : EncryptionKeys {
            override fun getOrCreate(): SecretKey = throw GeneralSecurityException("Keystore unavailable")
            override fun get(): SecretKey = throw GeneralSecurityException("Key missing")
        }
        val encryption = KeystoreEncryption(unavailable)
        assertFalse(encryption.init())
        assertThrows(GeneralSecurityException::class.java) { encryption.encrypt("key", "sensitive") }
    }

    @Test fun legacyValuesAreRejectedBeforeAccessingKeys() {
        val unavailable = object : EncryptionKeys {
            override fun getOrCreate(): SecretKey = error("Must not create a key")
            override fun get(): SecretKey = error("Must not access a key")
        }
        val encryption = KeystoreEncryption(unavailable)
        for (value in listOf("", "aGF3aw==", "old encrypted value")) {
            val error = assertThrows(IllegalArgumentException::class.java) { encryption.decrypt("key", value) }
            assertTrue(error.message!!.contains("migrate legacy values"))
        }
    }
}
