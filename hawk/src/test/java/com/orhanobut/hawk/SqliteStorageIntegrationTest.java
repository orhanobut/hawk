package com.orhanobut.hawk;

import android.content.Context;
import android.util.Pair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

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

  @Test public void putBulk() {
    List<Pair<String, ?>> list = new ArrayList<>();
    Pair<String, String> pair = new Pair<>("a1", "b1");
    Pair<String, String> pair2 = new Pair<>("a2", "b2");
    list.add(pair);
    list.add(pair2);

    storage.put(list);
    assertThat(storage.get("a1")).isEqualTo("b1");
    assertThat(storage.get("a2")).isEqualTo("b2");
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

    storage.remove("string");
    assertThat(storage.get("string")).isNull();
  }

  @Test public void removeInvalid() {
    assertThat(storage.remove(null, null)).isTrue();
    assertThat(storage.remove("")).isTrue();
  }

  @Test public void bulkRemove() {
    storage.put("a1", "string");
    storage.put("a2", "string");
    assertThat(storage.get("a1")).isNotNull();
    assertThat(storage.get("a2")).isNotNull();

    storage.remove("a1", "a2");
    assertThat(storage.get("a1")).isNull();
    assertThat(storage.get("a2")).isNull();
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
