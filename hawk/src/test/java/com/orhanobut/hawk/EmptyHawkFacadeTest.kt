package com.orhanobut.hawk

import org.junit.Test

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail

class EmptyHawkFacadeTest {

  private val hawkFacade = HawkFacade.EmptyHawkFacade()

  private fun assertFail(e: Exception) {
    assertThat(e).hasMessage("Hawk is not built. " + "Please call build() and wait the initialisation finishes.")
  }

  @Test fun put() {
    try {
      hawkFacade.put("key", "value")
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun get() {
    try {
      hawkFacade.get<Any>("key")
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun getWithDefault() {
    try {
      hawkFacade.get("key", "default")
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun count() {
    try {
      hawkFacade.count()
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun deleteAll() {
    try {
      hawkFacade.deleteAll()
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun delete() {
    try {
      hawkFacade.delete("key")
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun contains() {
    try {
      hawkFacade.contains("Key")
      fail("")
    } catch (e: Exception) {
      assertFail(e)
    }

  }

  @Test fun isBuilt() {
    assertThat(hawkFacade.isBuilt).isFalse()
  }
}