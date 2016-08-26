package com.orhanobut.hawk;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SqliteStorageTest {

  @Mock SqliteStorage.SqliteHelper helper;

  SqliteStorage storage;

  @Before public void setUp() throws Exception {
    initMocks(this);

    storage = new SqliteStorage(helper);
  }

  @Test public void putObject() throws Exception {
    storage.put("key", "value");

    verify(helper).put("key", "value");
  }

  @Test public void throwExceptionOnNullKeysPassedOnPut() {
    try {
      storage.put(null, "value");
      fail("key should not be null");
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null or empty");
    }
  }

  @Test public void get() throws Exception {
    storage.get("key");

    verify(helper).get("key");
  }

  @Test public void remove() throws Exception {
    storage.delete("key");

    verify(helper).delete("key");
  }

  @Test public void contains() throws Exception {
    storage.contains("key");

    verify(helper).contains("key");
  }

  @Test public void clear() throws Exception {
    storage.clear();

    verify(helper).clearAll();
  }

  @Test public void count() throws Exception {
    storage.count();

    verify(helper).count();
  }
}