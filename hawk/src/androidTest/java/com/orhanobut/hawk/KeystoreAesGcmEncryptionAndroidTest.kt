package com.orhanobut.hawk

import androidx.test.runner.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail

/**
 * Instrumentation test for the real Android Keystore path. Requires a device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class KeystoreAesGcmEncryptionAndroidTest {

  private lateinit var encryption: KeystoreAesGcmEncryption

  @Before fun setup() {
    InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences(KEY_ALIAS, android.content.Context.MODE_PRIVATE)
        .edit()
        .clear()
        .commit()
    encryption = KeystoreAesGcmEncryption(KEY_ALIAS)
  }

  @After fun tearDown() {
    // Best-effort cleanup of the generated key so tests stay isolated.
    try {
      val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
      keyStore.load(null)
      keyStore.deleteEntry(KEY_ALIAS)
    } catch (ignored: Exception) {
    }
  }

  @Test fun initCreatesKeyAndSucceeds() {
    assertThat(encryption.init()).isTrue()
  }

  @Test fun testEncryptAndDecrypt() {
    assertThat(encryption.init()).isTrue()

    val key = "key"
    val value = "value"

    val cipherText = encryption.encrypt(key, value)
    val plainValue = encryption.decrypt(key, cipherText)

    assertThat(plainValue).isEqualTo(value)
  }

  @Test fun decryptWithDifferentEntityFails() {
    assertThat(encryption.init()).isTrue()

    val cipherText = encryption.encrypt("key", "value")

    try {
      encryption.decrypt("different-key", cipherText)
      fail("decrypt should fail when the entity does not match")
    } catch (e: Exception) {
      // expected: AEADBadTagException (or similar)
    }
  }

  companion object {
    private const val KEY_ALIAS = "hawk-next-android-test-key"
  }
}
