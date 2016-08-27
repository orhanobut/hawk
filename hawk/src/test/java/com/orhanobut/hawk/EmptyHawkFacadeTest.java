package com.orhanobut.hawk;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;

public class EmptyHawkFacadeTest {

  HawkFacade hawkFacade = new HawkFacade.EmptyHawkFacade();

  private void assertFail(Exception e) {
    assertThat(e).hasMessage("Hawk is not built. " +
        "Please call build() and wait the initialisation finishes.");
  }

  @Test public void put() {
    try {
      hawkFacade.put("key", "value");
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void get() {
    try {
      hawkFacade.get("key");
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void getWithDefault() {
    try {
      hawkFacade.get("key", "default");
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void count() {
    try {
      hawkFacade.count();
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void deleteAll() {
    try {
      hawkFacade.deleteAll();
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void delete() {
    try {
      hawkFacade.delete("key");
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void contains() {
    try {
      hawkFacade.contains("Key");
      fail("");
    } catch (Exception e) {
      assertFail(e);
    }
  }

  @Test public void isBuilt() {
    assertThat(hawkFacade.isBuilt()).isFalse();
  }
}