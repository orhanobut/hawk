package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Orhan Obut
 */
public class HawkBackupTest extends InstrumentationTestCase {

    Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(
                "dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());
        context = getInstrumentation().getContext();
        Hawk.init(context);
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

    public void testBooleanDefault() {
        assertEquals(Boolean.FALSE, Hawk.get("tag", false));
    }

    public void testBooleanNotDefault() {
        Hawk.put("tag", true);
        assertNotSame(true, Hawk.get("tag", false));
    }

    public void testChar() {
        char expected = 'a';
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testCharDefault() {
        char expected = 'a';
        assertEquals(Character.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testCharNotDefault() {
        char expected = 'a';
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 'b'));
    }

    public void testByte() {
        byte expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testByteDefault() {
        byte expected = 0;
        assertEquals(Byte.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testByteNotDefault() {
        byte expected = 0;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 1));
    }

    public void testShort() {
        short expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testShortDefault() {
        short expected = 0;
        assertEquals(Short.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testShortNotDefault() {
        short expected = 0;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 1));
    }

    public void testInt() {
        int expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testIntDefault() {
        int expected = 0;
        assertEquals(Integer.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testIntNotDefault() {
        int expected = 0;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 1));
    }

    public void testLong() {
        long expected = 100L;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testLongDefault() {
        long expected = 100L;
        assertEquals(Long.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testLongNotDefault() {
        long expected = 100L;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 99L));
    }

    public void testFloat() {
        float expected = 0.1f;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testFloatDefault() {
        float expected = 0.1f;
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testFloatNotDefault() {
        float expected = 0.1f;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 0.9f));
    }

    public void testDouble() {
        double expected = 11;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testDoubleDefault() {
        double expected = 11;
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testDoubleNotDefault() {
        double expected = 11;
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 99));
    }

    public void testString() {
        String expected = "test";
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testStringDefault() {
        String expected = "test";
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testStringNotDefault() {
        String expected = "test";
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", "default"));
    }

    public void testSerializableObject() {
        FooSerializable foo = new FooSerializable();

        Hawk.put("tag", foo);
        FooSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testSerializableObjectDefault() {
        FooSerializable foo = new FooSerializable();

        FooSerializable foo1 = Hawk.get("tag", foo);

        assertNotNull(foo1);
    }

    public void testNotSerializableObject() {
        FooNotSerializable foo = new FooNotSerializable();

        Hawk.put("tag", foo);
        FooNotSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testNotSerializableObjectDefault() {
        FooNotSerializable foo = new FooNotSerializable();

        FooNotSerializable foo1 = Hawk.get("tag", foo);

        assertNotNull(foo1);
    }

    public void testParcelableObject() {
        FooParcelable foo = new FooParcelable();
        Hawk.put("tag", foo);
        FooParcelable fooParcelable = Hawk.get("tag");

        assertNotNull(fooParcelable);
    }

    public void testParcelableObjectDefault() {
        FooParcelable foo = new FooParcelable();

        FooParcelable fooParcelable = Hawk.get("tag", foo);

        assertNotNull(fooParcelable);
    }

    public void testListSerializable() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        Hawk.put("tag", list);

        List<String> list1 = Hawk.get("tag");

        assertNotNull(list1);
    }

    public void testListSerializableDefault() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        List<String> list1 = Hawk.get("tag", list);

        assertNotNull(list1);
    }

    public void testListParcelable() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        Hawk.put("tag", list);

        List<FooParcelable> list1 = Hawk.get("tag");

        assertNotNull(list1);
    }

    public void testListParcelableDefault() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        List<FooParcelable> list1 = Hawk.get("tag", list);

        assertNotNull(list1);
    }

    public void testEmptyList() {
        try {
            List<FooParcelable> list = new ArrayList<>();
            Hawk.put("tag", list);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
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

    public void testNullKeyPutList() {
        try {
            Hawk.put(null, new ArrayList<String>());
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testNullValuePut() {
        try {
            Hawk.put("tag", null);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    public void testCount() {
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
