package com.orhanobut.hawk

import org.junit.Assert.*

import org.junit.Test



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
      assertEquals("foo should not be null", e.message)
    }
  }

  @Test fun checkNullOrEmptyThrowException() {
    try {
      HawkUtils.checkNullOrEmpty("foo", null)
      fail("should throw exception")
    } catch (e: Exception) {
      assertEquals("foo should not be null or empty", e.message)
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
    assertTrue(HawkUtils.isEmpty(null))
    assertTrue(HawkUtils.isEmpty(""))
    assertTrue(HawkUtils.isEmpty(" "))
    assertFalse(HawkUtils.isEmpty("foo"))
  }
}
