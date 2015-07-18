package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Orhan Obut
 */
public class SqliteStorageTest extends InstrumentationTestCase {

  private Context context;
  private Storage helper;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();
    helper = new SqliteStorage(context);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
    helper.clear();
  }

  public void testClearAll() {
    helper.put("a1", "String");
    helper.put("a2", "String");
    assertNotNull(helper.get("a1"));
    assertNotNull(helper.get("a2"));

    helper.clear();
    assertNull(helper.get("a1"));
    assertNull(helper.get("a2"));
  }

  public void testPut() {
    helper.put("string", "String");
    assertEquals("String", helper.get("string"));
  }

  public void testPutBulk() {
    List<Pair<String, ?>> list = new ArrayList<>();
    Pair<String, String> pair = new Pair<>("a1", "b1");
    Pair<String, String> pair2 = new Pair<>("a2", "b2");
    list.add(pair);
    list.add(pair2);

    helper.put(list);
    assertEquals("b1", helper.get("a1"));
    assertEquals("b2", helper.get("a2"));
  }

  public void testContains() {
    helper.put("string", "String");
    assertTrue(helper.contains("string"));
  }

  public void testDelete() {
    helper.put("string", "string");
    assertNotNull(helper.get("string"));

    helper.remove("string");
    assertNull(helper.get("string"));
  }

  public void testBulkDelete() {
    helper.put("a1", "string");
    helper.put("a2", "string");
    assertNotNull(helper.get("a1"));
    assertNotNull(helper.get("a2"));

    helper.remove("a1", "a2");
    assertNull(helper.get("a1"));
    assertNull(helper.get("a2"));
  }

  public void testCount() {
    helper.put("a1", "string");
    helper.put("a2", "string");
    assertNotNull(helper.get("a1"));
    assertNotNull(helper.get("a2"));

    assertEquals(2, helper.count());
  }

  public void testUpdate() {
    helper.put("a1", "b1");
    assertEquals("b1", helper.get("a1"));

    helper.put("a1", "b2");
    assertEquals("b2", helper.get("a1"));

  }
}
