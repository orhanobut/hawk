package com.orhanobut.hawk

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

class HawkTest {

  @Mock private lateinit var hawkFacade: HawkFacade

  @Before fun setup() {
    initMocks(this)

    Hawk.hawkFacade = hawkFacade
  }

  @After fun tearDown() {
    Hawk.destroy()
  }

  @Test fun testInit() {
    try {
      Hawk.init(null)
      fail("context should not be null")
    } catch (e: Exception) {
      assertThat(e).hasMessage("Context should not be null")
    }

  }

  @Test fun put() {
    Hawk.put("key", "value")

    verify(hawkFacade).put("key", "value")
  }

  @Test fun get() {
    Hawk.get<Any>("key")

    verify(hawkFacade).get<Any>("key")
  }

  @Test fun getWithDefault() {
    Hawk.get("key", "default")

    verify(hawkFacade).get("key", "default")
  }

  @Test fun count() {
    Hawk.count()

    verify(hawkFacade).count()
  }

  @Test fun deleteAll() {
    Hawk.deleteAll()

    verify(hawkFacade).deleteAll()
  }

  @Test fun delete() {
    Hawk.delete("key")

    verify(hawkFacade).delete("key")
  }

  @Test fun contains() {
    Hawk.contains("key")

    verify(hawkFacade).contains("key")
  }

  @Test fun isBuilt() {
    Hawk.isBuilt()

    verify(hawkFacade).isBuilt
  }

}
