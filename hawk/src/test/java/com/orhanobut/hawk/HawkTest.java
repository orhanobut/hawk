package com.orhanobut.hawk;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class HawkTest {

  @Mock HawkFacade hawkFacade;
  @Mock Context context;

  @Before public void setup() {
    initMocks(this);

    Hawk.HAWK_FACADE = hawkFacade;
  }

  @After public void tearDown() {
    Hawk.destroy();
  }

  @Test public void testInit() {
    try {
      Hawk.init(null);
      fail("context should not be null");
    } catch (Exception e) {
      assertThat(e).hasMessage("Context should not be null");
    }
  }

  @Test public void put() {
    Hawk.put("key", "value");

    verify(hawkFacade).put("key", "value");
  }

  @Test public void get() {
    Hawk.get("key");

    verify(hawkFacade).get("key");
  }

  @Test public void getWithDefault() {
    Hawk.get("key", "default");

    verify(hawkFacade).get("key", "default");
  }

  @Test public void count() {
    Hawk.count();

    verify(hawkFacade).count();
  }

  @Test public void deleteAll() {
    Hawk.deleteAll();

    verify(hawkFacade).deleteAll();
  }

  @Test public void delete() {
    Hawk.delete("key");

    verify(hawkFacade).delete("key");
  }

  @Test public void contains() {
    Hawk.contains("key");

    verify(hawkFacade).contains("key");
  }

  @Test public void isBuilt() {
    Hawk.isBuilt();

    verify(hawkFacade).isBuilt();
  }

}
