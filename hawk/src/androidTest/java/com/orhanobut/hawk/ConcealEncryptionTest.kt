package com.orhanobut.hawk

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.facebook.soloader.SoLoader

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class ConcealEncryptionTest {

  private lateinit var encryption: Encryption

  @Before fun setup() {
    encryption = ConcealEncryption(InstrumentationRegistry.getContext())
  }

  @Before fun prepareSo() {
    SoLoader.init(InstrumentationRegistry.getContext(), false)
  }

  @Test fun init() {
    assertThat(encryption.init()).isTrue()
  }

  @Test fun testEncryptAndDecrypt() {
    val key = "key"
    val value = "value"

    val cipherText = encryption.encrypt(key, value)

    val plainValue = encryption.decrypt(key, cipherText)

    assertThat(plainValue).isEqualTo(value)
  }
}