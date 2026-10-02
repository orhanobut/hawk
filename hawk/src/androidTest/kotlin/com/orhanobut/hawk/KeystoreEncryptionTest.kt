package com.orhanobut.hawk

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import java.security.KeyStore
import javax.crypto.KeyGenerator
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KeystoreEncryptionTest {
    private val alias = "hawk.test.aes-gcm"
    private fun store() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    @Before fun clearKey() { store().deleteEntry(alias) }
    @After fun cleanup() { clearKey() }

    @Test fun keystoreKeyPersistsAcrossInstancesAndIsNotExportable() {
        val first = KeystoreEncryption(alias)
        assertTrue(first.init())
        val encrypted = first.encrypt("key", "héllo 🦅")
        val second = KeystoreEncryption(alias)
        assertTrue(second.init())
        assertEquals("héllo 🦅", second.decrypt("key", encrypted))
        assertNull(store().getKey(alias, null).encoded)
        assertFalse(encrypted.contains("héllo"))
    }

    @Test fun authenticatesKeyCiphertextAndNonceOnAndroid() {
        val encryption = KeystoreEncryption(alias)
        assertTrue(encryption.init())
        for (value in listOf("", "héllo 🦅", "x".repeat(5000))) {
            assertEquals(value, encryption.decrypt("key", encryption.encrypt("key", value)))
        }
        val encrypted = encryption.encrypt("key", "value")
        assertNotEquals(encrypted, encryption.encrypt("key", "value"))
        assertThrows(Exception::class.java) { encryption.decrypt("other-key", encrypted) }
        val prefix = "hawk-aes-gcm:"
        for (offset in listOf(1, 13, Base64.decode(encrypted.removePrefix(prefix), Base64.NO_WRAP).lastIndex)) {
            val bytes = Base64.decode(encrypted.removePrefix(prefix), Base64.NO_WRAP)
            bytes[offset] = (bytes[offset].toInt() xor 1).toByte()
            assertThrows(Exception::class.java) {
                encryption.decrypt("key", prefix + Base64.encodeToString(bytes, Base64.NO_WRAP))
            }
        }
    }

    @Test fun missingKeyReadFailsWithoutCreatingReplacement() {
        val encryption = KeystoreEncryption(alias)
        assertTrue(encryption.init())
        val encrypted = encryption.encrypt("key", "value")
        clearKey()
        assertThrows(Exception::class.java) { encryption.decrypt("key", encrypted) }
        assertFalse(store().containsAlias(alias))
    }

    @Test fun malformedAndLegacyValuesAreRejected() {
        val encryption = KeystoreEncryption(alias)
        assertTrue(encryption.init())
        for (value in listOf("old encrypted value", "hawk-aes-gcm:!", "hawk-aes-gcm:AA==")) {
            assertThrows(IllegalArgumentException::class.java) { encryption.decrypt("key", value) }
        }
        assertThrows(IllegalArgumentException::class.java) { KeystoreEncryption(" ") }
    }

    @Test fun defaultBuilderFailsClosedWhenAliasHasIncompatibleKey() {
        val defaultAlias = "com.orhanobut.hawk.aes-gcm"
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        store().deleteEntry(defaultAlias)
        try {
            KeyGenerator.getInstance("HmacSHA256", "AndroidKeyStore").apply {
                init(KeyGenParameterSpec.Builder(defaultAlias, KeyProperties.PURPOSE_SIGN)
                    .setKeySize(256).build())
            }.generateKey()
            assertThrows(IllegalStateException::class.java) { Hawk.init(context).build() }
            assertFalse(Hawk.isBuilt())
        } finally {
            store().deleteEntry(defaultAlias)
        }
    }
}
