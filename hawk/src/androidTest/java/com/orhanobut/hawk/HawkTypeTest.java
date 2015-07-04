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
public class HawkTypeTest extends InstrumentationTestCase {

  Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();
    Hawk.init(context, "testPassword");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
    Hawk.clear();
    Hawk.resetCrypto();
  }

  public void testBoolean() {
    Hawk.put("tag", true);
    assertEquals(true, Hawk.get("tag"));
  }

  public void testString() {
    String expected = "test";
    Hawk.put("tag", expected);
    assertEquals(expected, Hawk.get("tag"));
  }

  public void testObject() {
    FooSerializable foo = new FooSerializable();

    Hawk.put("tag", foo);
    FooSerializable foo1 = Hawk.get("tag");

    assertNotNull(foo1);
  }

  public void testList() {
    List<FooNotSerializable> list = new ArrayList<>();
    list.add(new FooNotSerializable());
    list.add(new FooNotSerializable());

    Hawk.put("tag", list);

    List<FooNotSerializable> list1 = Hawk.get("tag");

    assertNotNull(list1);
  }

  public void testMap() {
    Map<String, FooBar> map = new HashMap<>();
    map.put("a", new FooBar());
    map.put("b", new FooBar());

    Hawk.put("tag", map);

    Map<String, FooBar> map1 = Hawk.get("tag");

    assertNotNull(map1);
  }

  public void testSet() {
    Set<FooBar> map = new HashSet<>();
    map.add(new FooBar());
    map.add(new FooBar());

    Hawk.put("tag", map);

    Set<String> map1 = Hawk.get("tag");

    assertNotNull(map1);
  }
}
