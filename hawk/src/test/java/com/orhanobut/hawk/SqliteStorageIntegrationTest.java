package com.orhanobut.hawk;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SqliteStorageIntegrationTest {

  Context context;
  Storage storage;

  @Before public void setUp() throws Exception {
    context = RuntimeEnvironment.application;

    storage = new SqliteStorage(
        new SqliteStorage.SqliteHelper(context, "TestHawk")
    );
  }

  @After public void tearDown() {
    storage.clear();
  }

  @Test public void init() {
    assertThat(context).isNotNull();
    assertThat(storage).isNotNull();
  }

  @Test public void clearAll() {
    storage.put("a1", "String");
    storage.put("a2", "String");

    assertThat(storage.get("a1")).isNotNull();
    assertThat(storage.get("a2")).isNotNull();

    storage.clear();
    assertThat(storage.get("a1")).isNull();
    assertThat(storage.get("a2")).isNull();
  }

  @Test public void put() {
    storage.put("string", "String");
    assertThat(storage.get("string")).isEqualTo("String");
  }

  @Test public void putInvalidValues() {
    try {
      storage.put(null, "test");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null or empty");
    }
    try {
      storage.put("", "test");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null or empty");
    }
  }

  @Test public void getInvalidValues() {
    try {
      storage.get(null);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null or empty");
    }
    try {
      storage.get("");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null or empty");
    }
  }

  @Test public void contains() {
    storage.put("string", "String");

    assertThat(storage.contains("string")).isTrue();
    assertThat(storage.contains(null)).isFalse();
  }

  @Test public void remove() {
    storage.put("string", "string");

    assertThat(storage.get("string")).isNotNull();

    storage.delete("string");
    assertThat(storage.get("string")).isNull();
  }

  @Test public void removeInvalid() {
    assertThat(storage.delete("")).isTrue();
  }

  @Test public void count() {
    storage.put("a1", "string");
    storage.put("a2", "string");
    assertThat(storage.get("a1")).isNotNull();
    assertThat(storage.get("a2")).isNotNull();

    assertThat(storage.count()).isEqualTo(2);
  }

  @Test public void update() {
    storage.put("a1", "b1");
    assertThat(storage.get("a1")).isEqualTo("b1");

    storage.put("a1", "b2");
    assertThat(storage.get("a1")).isEqualTo("b2");
  }
}
