package com.orhanobut.hawk;

import android.util.Pair;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
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

  @Test public void putList() throws Exception {
    List<Pair<String, ?>> list = new ArrayList<>();
    list.add(new Pair<String, Object>("f1", "s1"));
    list.add(new Pair<String, Object>("f2", "s2"));

    storage.put(list);

    verify(helper).put(list);
  }

  @Test public void get() throws Exception {
    storage.get("key");

    verify(helper).get("key");
  }

  @Test public void remove() throws Exception {
    storage.remove("key");

    verify(helper).delete("key");
  }

  @Test public void removeMultiple() throws Exception {
    storage.remove("k1", "k2");

    verify(helper).delete("k1", "k2");
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