package com.orhanobut.hawk

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class HawkUtilsTest {

  @Test fun checkNullShouldDoNothing() {
    try {
      HawkUtils.checkNull("foo", "test")
    } catch (e: Exception) {
      fail("it should not throw exception")
    }

  }

  @Test fun checkNullShouldThrowException() {
    try {
      HawkUtils.checkNull("foo", null)
      fail("should throw exception")
    } catch (e: Exception) {
      assertThat(e).hasMessage("foo should not be null")
    }
  }

  @Test fun checkNullOrEmptyThrowException() {
    try {
      HawkUtils.checkNullOrEmpty("foo", null)
      fail("should throw exception")
    } catch (e: Exception) {
      assertThat(e).hasMessage("foo should not be null or empty")
    }
  }

  @Test fun checkNullOrEmptyShouldDoNothing() {
    try {
      HawkUtils.checkNullOrEmpty("foo", "bar")
    } catch (e: Exception) {
      fail("should not throw exception")
    }
  }

  @Test fun isEmpty() {
    assertThat(HawkUtils.isEmpty(null)).isTrue()
    assertThat(HawkUtils.isEmpty("")).isTrue()
    assertThat(HawkUtils.isEmpty(" ")).isTrue()
    assertThat(HawkUtils.isEmpty("foo")).isFalse()
  }
}