package com.orhanobut.hawk;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HawkIntegrationTest {

  static final String KEY = "TAG";
  static final long LATCH_TIMEOUT_IN_SECONDS = 3;

  Context context;

  @Before public void setUp() {
    context = RuntimeEnvironment.application;
    Hawk.init(context).build();
  }

  @After public void tearDown() {
    if (Hawk.isBuilt()) {
      Hawk.clear();
    }
    Hawk.destroy();
  }

  @Test public void testSingleItem() {
    Hawk.put("boolean", true);
    assertThat(Hawk.get("boolean")).isEqualTo(true);

    Hawk.put("string", "string");
    assertThat(Hawk.get("string")).isEqualTo("string");

    Hawk.put("float", 1.5f);
    assertThat(Hawk.get("float")).isEqualTo(1.5f);

    Hawk.put("integer", 10);
    assertThat(Hawk.get("integer")).isEqualTo(10);

    Hawk.put("char", 'A');
    assertThat(Hawk.get("char")).isEqualTo('A');

    Hawk.put("object", new FooBar());
    FooBar fooBar = Hawk.get("object");

    assertThat(fooBar).isNotNull();
    assertThat(fooBar.name).isEqualTo("hawk");

    assertThat(Hawk.put("innerClass", new FooBar.InnerFoo())).isTrue();
    FooBar.InnerFoo innerFoo = Hawk.get("innerClass");
    assertThat(innerFoo).isNotNull();
    assertThat(innerFoo.name).isEqualTo("hawk");
  }

  @Test public void testSingleItemDefault() {
    boolean result = Hawk.get("tag", true);
    assertThat(result).isEqualTo(true);
  }

  @Test public void testList() {
    List<String> list = new ArrayList<>();
    list.add("foo");
    list.add("bar");

    Hawk.put("tag", list);

    List<String> list1 = Hawk.get("tag");

    assertThat(list1).isNotNull();
    assertThat(list1.get(0)).isEqualTo("foo");
    assertThat(list1.get(1)).isEqualTo("bar");
  }

  @Test public void testEmptyList() {
    List<FooBar> list = new ArrayList<>();
    Hawk.put("tag", list);

    List<FooBar> list1 = Hawk.get("tag");

    assertThat(list1).isNotNull();
  }

  @Test public void testMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    Hawk.put("map", map);

    Map<String, String> map1 = Hawk.get("map");

    assertThat(map1).isNotNull();
    assertThat(map1.get("key")).isEqualTo("value");
  }

  @Test public void testEmptyMap() {
    Map<String, FooBar> map = new HashMap<>();
    Hawk.put("tag", map);

    Map<String, FooBar> map1 = Hawk.get("tag");

    assertThat(map1).isNotNull();
  }

  @Test public void testSet() {
    Set<String> set = new HashSet<>();
    set.add("foo");
    Hawk.put("set", set);

    Set<String> set1 = Hawk.get("set");

    assertThat(set1).isNotNull();
    assertThat(set1.contains("foo")).isTrue();
  }

  @Test public void testEmptySet() {
    Set<FooBar> set = new HashSet<>();
    Hawk.put("tag", set);

    Set<FooBar> set1 = Hawk.get("tag");

    assertThat(set1).isNotNull();
  }

  @Test public void testCount() {
    Hawk.clear();
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);
    Hawk.put("tag3", value);
    Hawk.put("tag4", value);

    assertThat(Hawk.count()).isEqualTo(5);
  }

  @Test public void testClear() {
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);

    Hawk.clear();

    assertThat(Hawk.count()).isEqualTo(0);
  }

  @Test public void testRemove() {
    Hawk.clear();
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);

    Hawk.delete("tag");

    String result = Hawk.get("tag");

    assertThat(result).isNull();
    assertThat(Hawk.count()).isEqualTo(2);
  }

  @Test public void testBulkRemoval() {
    Hawk.clear();
    Hawk.put("tag", "test");
    Hawk.put("tag1", 1);
    Hawk.put("tag2", Boolean.FALSE);

    Hawk.delete("tag", "tag1");

    String result = Hawk.get("tag");

    assertThat(result).isNull();
    assertThat(Hawk.count()).isEqualTo(1);
  }

  @Test public void testContains() {
    String value = "test";
    String key = "tag";
    Hawk.put(key, value);

    assertThat(Hawk.contains(key)).isTrue();

    Hawk.delete(key);

    assertThat(Hawk.contains(key)).isFalse();
  }

  @Test public void testChain() {
    Hawk.chain()
        .put("tag", 1)
        .put("tag1", "yes")
        .put("tag2", Boolean.FALSE)
        .commit();

    assertThat(Hawk.get("tag")).isEqualTo(1);
    assertThat(Hawk.get("tag1")).isEqualTo("yes");
    assertThat(Hawk.get("tag2")).isEqualTo(false);
  }

  @Test public void testChainWithCapacity() {
    Hawk.chain(10)
        .put("tag", 1)
        .put("tag1", "yes")
        .put("tag2", Boolean.FALSE)
        .commit();

    assertThat(Hawk.get("tag")).isEqualTo(1);
    assertThat(Hawk.get("tag1")).isEqualTo("yes");
    assertThat(Hawk.get("tag2")).isEqualTo(false);
  }

  @Test public void testChainWithLists() {
    List<String> items = new ArrayList<>();
    items.add("fst");
    items.add("snd");
    items.add("trd");

    Hawk.chain()
        .put("tag", 1)
        .put("tag1", "yes")
        .put("tag2", Boolean.FALSE)
        .put("lst", items)
        .commit();

    assertThat(Hawk.get("tag")).isEqualTo(1);
    assertThat(Hawk.get("tag1")).isEqualTo("yes");
    assertThat(Hawk.get("tag2")).isEqualTo(false);

    List<String> stored = Hawk.get("lst");

    assertThat(stored).isNotNull();
    assertThat(stored.isEmpty()).isFalse();

    for (int i = 0, s = stored.size(); i < s; i++) {
      assertThat(stored.get(i)).isEqualTo(items.get(i));
    }
  }

  @Test public void testHugeData() {
    for (int i = 0; i < 100; i++) {
      Hawk.put("" + i, "" + i);
    }
    assertThat(true).isTrue();
  }

  @Test public void testHugeDataWithBulk() {
    Hawk.Chain chain = Hawk.chain();
    for (int i = 0; i < 10000; i++) {
      chain.put("" + i, "" + i);
    }
    chain.commit();
    assertThat(true).isTrue();
  }

  @Test public void resetCrypto() {
    assertThat(Hawk.resetCrypto()).isTrue();
  }

}
