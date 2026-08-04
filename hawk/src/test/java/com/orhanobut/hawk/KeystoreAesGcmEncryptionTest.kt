package com.orhanobut.hawk

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RunWith(RobolectricTestRunner::class)
class KeystoreAesGcmEncryptionTest {

  private fun newKey(): SecretKey =
    KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

  private fun encryptionWith(key: SecretKey): KeystoreAesGcmEncryption =
    KeystoreAesGcmEncryption("test-alias", key)

  @Test fun encryptDecryptRoundTrip() {
    val encryption = encryptionWith(newKey())

    val cipherText = encryption.encrypt("key", "value")

    assertThat(cipherText).isNotNull()
    assertThat(encryption.decrypt("key", cipherText)).isEqualTo("value")
  }

  @Test fun encryptUsesRandomIvPerCall() {
    val encryption = encryptionWith(newKey())

    val first = encryption.encrypt("key", "value")
    val second = encryption.encrypt("key", "value")

    assertThat(first).isNotEqualTo(second)
  }

  @Test fun decryptWithWrongKeyFails() {
    val cipherText = encryptionWith(newKey()).encrypt("key", "value")
    val wrongKeyEncryption = encryptionWith(newKey())

    try {
      wrongKeyEncryption.decrypt("key", cipherText)
      fail("decrypt with the wrong key should fail")
    } catch (e: Exception) {
      // expected
    }
  }

  @Test fun decryptWithDifferentEntityFails() {
    val encryption = encryptionWith(newKey())
    val cipherText = encryption.encrypt("key", "value")

    try {
      encryption.decrypt("different-key", cipherText)
      fail("decrypt with a different entity should fail")
    } catch (e: Exception) {
      // expected
    }
  }

  @Test fun decryptCorruptedDataFails() {
    val encryption = encryptionWith(newKey())
    val cipherText = encryption.encrypt("key", "value")
    val decoded = Base64.getDecoder().decode(cipherText)
    decoded[decoded.size - 1] = (decoded.last().toInt() xor 0x01).toByte()
    val corrupted = Base64.getEncoder().encodeToString(decoded)

    try {
      encryption.decrypt("key", corrupted)
      fail("decrypt of corrupted data should fail")
    } catch (e: Exception) {
      // expected
    }
  }

  @Test fun decryptTooShortFails() {
    val encryption = encryptionWith(newKey())
    val tooShort = Base64.getEncoder().encodeToString("hello".toByteArray())

    try {
      encryption.decrypt("key", tooShort)
      fail("decrypt of a truncated payload should fail")
    } catch (e: Exception) {
      // expected
    }
  }

  @Test fun encryptNullValueReturnsNull() {
    val encryption = encryptionWith(newKey())
    assertThat(encryption.encrypt("key", null)).isNull()
    assertThat(encryption.decrypt("key", null)).isNull()
  }

  @Test fun initFailsWithoutAndroidKeyStore() {
    // Unit tests run on the JVM where the AndroidKeyStore provider does not exist.
    assertThat(KeystoreAesGcmEncryption("missing-provider").init()).isFalse()
  }
}
