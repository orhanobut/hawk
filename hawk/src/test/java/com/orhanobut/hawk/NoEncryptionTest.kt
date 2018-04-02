package com.orhanobut.hawk

import android.util.Base64

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import com.google.common.truth.Truth.assertThat
import org.mockito.Matchers.any
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

@RunWith(RobolectricTestRunner::class)
class NoEncryptionTest {

  @Spy private lateinit var encryption: NoEncryption

  @Before fun setup() {
    encryption = NoEncryption()

    initMocks(this)
  }

  @Test fun init() {
    assertThat(encryption.init()).isTrue()
  }

  @Test fun encrypt() {
    encryption.encrypt("key", "value")

    verify(encryption).encodeBase64(any(ByteArray::class.java))
  }

  @Test fun decrypt() {
    encryption.decrypt("key", "test")

    verify(encryption).decrypt("key", "test")
  }

  @Test fun encodeBase64() {
    val text = "hawk"
    val expected = Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
    val actual = encryption.encodeBase64(text.toByteArray())

    assertThat(actual).isNotNull()
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun decodeBase64() {
    val text = "hawk"
    val expected = Base64.decode(text, Base64.DEFAULT)
    val actual = encryption.decodeBase64(text)

    assertThat(actual).isNotNull()
    assertThat(actual).isEqualTo(expected)
  }
}