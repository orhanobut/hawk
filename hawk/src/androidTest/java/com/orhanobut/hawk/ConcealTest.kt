package com.orhanobut.hawk

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.CryptoConfig
import com.facebook.crypto.Entity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConcealTest {

  private lateinit var crypto: Crypto

  @Before fun setup() {
    val context = InstrumentationRegistry.getContext()
    val keyChain = SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256)
    crypto = AndroidConceal.get().createDefaultCrypto(keyChain)
  }

  @Test fun cryptoIsAvailable() {
    assertThat(crypto.isAvailable).isTrue()
  }

  @Test fun testConceal() {
    val entity = Entity.create("key")
    val value = "value"
    val encryptedValue = crypto.encrypt(value.toByteArray(), entity)

    assertThat(encryptedValue).isNotNull()

    val decryptedValue = String(crypto.decrypt(encryptedValue, entity))
    assertThat(decryptedValue).isEqualTo("value")
  }
}
