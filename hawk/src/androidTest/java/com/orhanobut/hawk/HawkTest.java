package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Orhan Obut
 */
public class HawkTest extends InstrumentationTestCase {

  Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();
    Hawk.init(context).build();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
    Hawk.clear();
  }

  public void testSingleItem() {
    Hawk.put("boolean", true);
    assertEquals(true, Hawk.get("boolean"));

    Hawk.put("string", "string");
    assertEquals("string", Hawk.get("string"));

    Hawk.put("float", 1.5f);
    assertEquals(1.5f, Hawk.get("float"));

    Hawk.put("integer", 10);
    assertEquals(10, Hawk.get("integer"));

    Hawk.put("char", 'A');
    assertEquals('A', Hawk.get("char"));

    Hawk.put("object", new FooBar());
    FooBar fooBar = Hawk.get("object");
    assertNotNull(fooBar);
    assertEquals("hawk", fooBar.name);
  }

  public void testSingleItemDefault() {
    boolean result = Hawk.get("tag", true);
    assertTrue(result);
  }

  public void testList() {
    List<String> list = new ArrayList<>();
    list.add("foo");
    list.add("bar");

    Hawk.put("tag", list);

    List<String> list1 = Hawk.get("tag");
    assertNotNull(list1);
    assertEquals("foo", list1.get(0));
    assertEquals("bar", list1.get(1));
  }

  public void testEmptyList() {
    List<FooBar> list = new ArrayList<>();
    Hawk.put("tag", list);

    List<FooBar> list1 = Hawk.get("tag");
    assertNotNull(list1);
  }

  public void testMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    Hawk.put("map", map);

    Map<String, String> map1 = Hawk.get("map");
    assertNotNull(map);
    assertEquals("value", map1.get("key"));
  }

  public void testEmptyMap() {
    Map<String, FooBar> map = new HashMap<>();
    Hawk.put("tag", map);

    Map<String, FooBar> map1 = Hawk.get("tag");
    assertNotNull(map1);
  }

  public void testSet() {
    Set<String> set = new HashSet<>();
    set.add("foo");
    Hawk.put("set", set);

    Set<String> set1 = Hawk.get("set");
    assertNotNull(set1);
    assertTrue(set1.contains("foo"));
  }

  public void testEmptySet() {
    Set<FooBar> set = new HashSet<>();
    Hawk.put("tag", set);

    Set<FooBar> set1 = Hawk.get("tag");
    assertNotNull(set1);
  }

  public void testNullKeyPut() {
    try {
      Hawk.put(null, "test");
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  public void testNullKeyGet() {
    try {
      Hawk.get(null);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  public void testNullValuePut() {
    try {
      Hawk.put("tag", "something");
      assertNotNull(Hawk.get("tag"));

      assertTrue(Hawk.put("tag", null));
      assertNull(Hawk.get("tag"));
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  public void testCount() {
    Hawk.clear();
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);
    Hawk.put("tag3", value);
    Hawk.put("tag4", value);

    int expected = 5;
    assertEquals(expected, Hawk.count());
  }

  public void testClear() {
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);

    Hawk.clear();
    int expected = 0;

    assertEquals(expected, Hawk.count());
  }

  public void testRemove() {
    Hawk.clear();
    String value = "test";
    Hawk.put("tag", value);
    Hawk.put("tag1", value);
    Hawk.put("tag2", value);

    Hawk.remove("tag");

    String result = Hawk.get("tag");

    assertNull(result);
    assertEquals(2, Hawk.count());
  }

  public void testBulkRemoval() {
    Hawk.clear();
    Hawk.put("tag", "test");
    Hawk.put("tag1", 1);
    Hawk.put("tag2", Boolean.FALSE);

    Hawk.remove("tag", "tag1");

    String result = Hawk.get("tag");

    assertNull(result);
    assertEquals(1, Hawk.count());
  }

  public void testContains() {
    String value = "test";
    String key = "tag";
    Hawk.put(key, value);

    assertTrue(Hawk.contains(key));

    Hawk.remove(key);

    assertFalse(Hawk.contains(key));
  }

  public void testChain() {
    Hawk.chain()
        .put("tag", 1)
        .put("tag1", "yes")
        .put("tag2", Boolean.FALSE)
        .commit();

    assertEquals(1, Hawk.get("tag"));
    assertEquals("yes", Hawk.get("tag1"));
    assertEquals(false, Hawk.get("tag2"));
  }

  public void testChainWithLists() {
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

    assertEquals(1, Hawk.get("tag"));
    assertEquals("yes", Hawk.get("tag1"));
    assertEquals(false, Hawk.get("tag2"));

    List<String> stored = Hawk.get("lst");
    assertNotNull(stored);
    assertFalse(stored.isEmpty());

    for (int i = 0, s = stored.size(); i < s; i++) {
      assertEquals(items.get(i), stored.get(i));
    }
  }


}
