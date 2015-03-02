package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.List;

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
        Hawk.init(context, "testPassword");
        Hawk.clear();
        Hawk.resetCrypto();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        context = null;
    }

    public void testBoolean() {
        Hawk.put("tag", true);
        assertEquals(true, Hawk.get("tag"));
    }

    public void testBooleanDefault() {
        assertEquals(Boolean.FALSE, Hawk.get("tag", false));
    }

    public void testBooleanDefaultCallback() {
        Hawk.get("tag", false, new Hawk.Callback<Boolean>() {

            @Override
            public void onSuccess(Boolean value) {
                assertEquals(Boolean.FALSE, value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get default value correctly");
            }
        });
    }

    public void testBooleanNotDefault() {
        Hawk.put("tag", true);
        assertFalse(Hawk.get("tag", false).equals(false));
    }

    public void testBooleanNotDefaultCallback() {
        Hawk.put("tag", true);
        Hawk.get("tag", false, new Hawk.Callback<Boolean>() {

            @Override
            public void onSuccess(Boolean value) {
                assertTrue(value.equals(Boolean.TRUE));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get default value correctly");
            }
        });
    }

    public void testChar() {
        char expected = 'a';
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testCharCallback() {
        final char expected = 'a';
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Character>() {

            @Override
            public void onSuccess(Character value) {
                assertEquals(Character.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get correctly");
            }
        });
    }

    public void testCharDefault() {
        char expected = 'a';
        assertEquals(Character.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testCharDefaultCallback() {
        final char expected = 'a';
        Hawk.get("tag", expected, new Hawk.Callback<Character>() {

            @Override
            public void onSuccess(Character value) {
                assertEquals(Character.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testCharNotDefault() {
        char expected = 'a';
        Hawk.put("tag", expected);
        assertNotSame(expected, Hawk.get("tag", 'b'));
    }

    public void testCharNotDefaultCallback() {
        final char expected = 'a';
        Hawk.put("tag", expected);
        Hawk.get("tag", 'b', new Hawk.Callback<Character>() {

            @Override
            public void onSuccess(Character value) {
                assertFalse(Character.valueOf(expected).equals(Character.valueOf('b')));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testByte() {
        byte expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testByteCallback() {
        final byte expected = 0;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Byte>() {
            @Override
            public void onSuccess(Byte o) {
                assertEquals(Byte.valueOf(expected), o);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testByteDefault() {
        byte expected = 0;
        assertEquals(Byte.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testByteDefaultCallback() {
        final byte expected = 0;
        Hawk.get("tag", expected, new Hawk.Callback<Byte>() {
            @Override
            public void onSuccess(Byte value) {
                assertEquals(Byte.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testByteNotDefault() {
        byte expected = 0;
        byte defaultValue = 1;
        Hawk.put("tag", expected);
        assertEquals(Byte.valueOf(expected), Hawk.get("tag", defaultValue));
    }

    public void testByteNotDefaultCallback() {
        final byte expected = 0;
        final byte defaultValue = 1;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Byte>() {
            @Override
            public void onSuccess(Byte value) {
                assertEquals(Byte.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testShort() {
        short expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testShortCallback() {
        final short expected = 0;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Short>() {
            @Override
            public void onSuccess(Short value) {
                assertEquals(Short.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testShortDefault() {
        short expected = 0;
        assertEquals(Short.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testShortDefaultCallback() {
        final short expected = 0;
        Hawk.get("tag", expected, new Hawk.Callback<Short>() {
            @Override
            public void onSuccess(Short value) {
                assertEquals(Short.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testShortNotDefault() {
        short expected = 0;
        short defaultValue = 1;
        Hawk.put("tag", expected);
        assertFalse(Hawk.get("tag", defaultValue).equals(defaultValue));
    }

    public void testShortNotDefaultCallback() {
        final short expected = 0;
        final short defaultValue = 1;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Short>() {
            @Override
            public void onSuccess(Short value) {
                assertFalse(Short.valueOf(defaultValue).equals(value));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testInt() {
        int expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testIntCallback() {
        final int expected = 0;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Integer>() {
            @Override
            public void onSuccess(Integer value) {
                assertEquals(Integer.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testIntDefault() {
        int expected = 0;
        assertEquals(Integer.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testIntDefaultCallback() {
        final int expected = 0;
        Hawk.get("tag", expected, new Hawk.Callback<Integer>() {
            @Override
            public void onSuccess(Integer value) {
                assertEquals(Integer.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testIntNotDefault() {
        int expected = 0;
        int defaultValue = 1;
        Hawk.put("tag", expected);
        assertFalse(Integer.valueOf(defaultValue).equals(Hawk.get("tag", defaultValue)));
    }

    public void testIntNotDefaultCallback() {
        final int expected = 0;
        final int defaultValue = 1;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Integer>() {
            @Override
            public void onSuccess(Integer value) {
                assertFalse(Integer.valueOf(defaultValue).equals(value));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testLong() {
        long expected = 100L;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testLongCallback() {
        final long expected = 100L;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Long>() {
            @Override
            public void onSuccess(Long value) {
                assertEquals(Long.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testLongDefault() {
        long expected = 100L;
        assertEquals(Long.valueOf(expected), Hawk.get("tag", expected));
    }

    public void testLongDefaultCallback() {
        final long expected = 100L;
        Hawk.get("tag", expected, new Hawk.Callback<Long>() {
            @Override
            public void onSuccess(Long value) {
                assertEquals(Long.valueOf(expected), value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testLongNotDefault() {
        long expected = 100L;
        long defaultValue = 99L;
        Hawk.put("tag", expected);
        assertFalse(Long.valueOf(defaultValue).equals(Hawk.get("tag", defaultValue)));
    }

    public void testLongNotDefaultCallback() {
        long expected = 100L;
        final long defaultValue = 99L;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Long>() {
            @Override
            public void onSuccess(Long value) {
                assertFalse(value.equals(defaultValue));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testFloat() {
        float expected = 0.1f;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testFloatCallback() {
        final float expected = 0.1f;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Float>() {
            @Override
            public void onSuccess(Float value) {
                assertEquals(expected, value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testFloatDefault() {
        float expected = 0.1f;
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testFloatDefaultCallback() {
        final float expected = 0.1f;
        Hawk.get("tag", expected, new Hawk.Callback<Float>() {
            @Override
            public void onSuccess(Float value) {
                assertEquals(expected, value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testFloatNotDefault() {
        float expected = 0.1f;
        final float defaultValue = 0.9f;
        Hawk.put("tag", expected);
        assertFalse(Hawk.get("tag", defaultValue).equals(Float.valueOf(defaultValue)));
    }

    public void testFloatNotDefaultCallback() {
        float expected = 0.1f;
        final float defaultValue = 0.9f;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Float>() {
            @Override
            public void onSuccess(Float value) {
                assertFalse(value.equals(Float.valueOf(defaultValue)));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testDouble() {
        double expected = 11;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testDoubleCallback() {
        final double expected = 11;
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<Double>() {
            @Override
            public void onSuccess(Double value) {
                assertEquals(value, expected);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testDoubleDefault() {
        double expected = 11;
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testDoubleDefaultCallback() {
        final double expected = 11;
        Hawk.get("tag", expected, new Hawk.Callback<Double>() {
            @Override
            public void onSuccess(Double value) {
                assertEquals(value, expected);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testDoubleNotDefault() {
        double expected = 11;
        double defaultValue = 99;
        Hawk.put("tag", expected);
        assertFalse(Hawk.get("tag", defaultValue).equals(defaultValue));
    }

    public void testDoubleNotDefaultCallback() {
        double expected = 11;
        final double defaultValue = 99;
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<Double>() {
            @Override
            public void onSuccess(Double value) {
                assertFalse(value.equals(defaultValue));
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testString() {
        String expected = "test";
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testStringCallback() {
        final String expected = "test";
        Hawk.put("tag", expected);
        Hawk.get("tag", new Hawk.Callback<String>() {
            @Override
            public void onSuccess(String value) {
                assertEquals(value, expected);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testStringDefault() {
        String expected = "test";
        assertEquals(expected, Hawk.get("tag", expected));
    }

    public void testStringDefaultCallback() {
        final String expected = "test";
        Hawk.get("tag", expected, new Hawk.Callback<String>() {
            @Override
            public void onSuccess(String value) {
                assertEquals(value, expected);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testStringNotDefault() {
        String expected = "test";
        String defaultValue = "default";
        Hawk.put("tag", expected);
        assertNotSame(defaultValue, Hawk.get("tag", "default"));
    }

    public void testStringNotDefaultCallback() {
        String expected = "test";
        final String defaultValue = "default";
        Hawk.put("tag", expected);
        Hawk.get("tag", defaultValue, new Hawk.Callback<String>() {
            @Override
            public void onSuccess(String value) {
                assertNotSame(value, defaultValue);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testSerializableObject() {
        FooSerializable foo = new FooSerializable();

        Hawk.put("tag", foo);
        FooSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testSerializableObjectCallback() {
        FooSerializable foo = new FooSerializable();

        Hawk.put("tag", foo);
        Hawk.get("tag", new Hawk.Callback<FooSerializable>() {
            @Override
            public void onSuccess(FooSerializable value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testSerializableObjectDefault() {
        FooSerializable foo = new FooSerializable();

        FooSerializable foo1 = Hawk.get("tag", foo);

        assertNotNull(foo1);
    }

    public void testSerializableObjectDefaultCallback() {
        FooSerializable foo = new FooSerializable();

        Hawk.get("tag", foo, new Hawk.Callback<FooSerializable>() {
            @Override
            public void onSuccess(FooSerializable value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testNotSerializableObject() {
        FooNotSerializable foo = new FooNotSerializable();

        Hawk.put("tag", foo);
        FooNotSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testNotSerializableObjectCallback() {
        FooNotSerializable foo = new FooNotSerializable();

        Hawk.put("tag", foo);
        Hawk.get("tag", new Hawk.Callback<FooNotSerializable>() {
            @Override
            public void onSuccess(FooNotSerializable value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testNotSerializableObjectDefault() {
        FooNotSerializable foo = new FooNotSerializable();

        FooNotSerializable foo1 = Hawk.get("tag", foo);

        assertNotNull(foo1);
    }

    public void testNotSerializableObjectDefaultCallback() {
        FooNotSerializable foo = new FooNotSerializable();

        Hawk.get("tag", foo, new Hawk.Callback<FooNotSerializable>() {
            @Override
            public void onSuccess(FooNotSerializable value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testParcelableObject() {
        FooParcelable foo = new FooParcelable();
        Hawk.put("tag", foo);
        FooParcelable fooParcelable = Hawk.get("tag");

        assertNotNull(fooParcelable);
    }

    public void testParcelableObjectCallback() {
        FooParcelable foo = new FooParcelable();
        Hawk.put("tag", foo);
        Hawk.get("tag", new Hawk.Callback<FooParcelable>() {
            @Override
            public void onSuccess(FooParcelable fooParcelable) {
                assertNotNull(fooParcelable);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testParcelableObjectDefault() {
        FooParcelable foo = new FooParcelable();

        FooParcelable fooParcelable = Hawk.get("tag", foo);

        assertNotNull(fooParcelable);
    }

    public void testParcelableObjectDefaultCallback() {
        FooParcelable foo = new FooParcelable();

        Hawk.get("tag", foo, new Hawk.Callback<FooParcelable>() {
            @Override
            public void onSuccess(FooParcelable value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testListSerializable() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        Hawk.put("tag", list);

        List<String> list1 = Hawk.get("tag");

        assertNotNull(list1);
    }

    public void testListSerializableCallback() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        Hawk.put("tag", list);

        Hawk.get("tag", new Hawk.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testListSerializableDefault() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        List<String> list1 = Hawk.get("tag", list);

        assertNotNull(list1);
    }

    public void testListSerializableDefaultCallback() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("foo");

        Hawk.get("tag", list, new Hawk.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testListParcelable() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        Hawk.put("tag", list);

        List<FooParcelable> list1 = Hawk.get("tag");

        assertNotNull(list1);
    }

    public void testListParcelableCallback() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        Hawk.put("tag", list);

        Hawk.get("tag", new Hawk.Callback<List<FooParcelable>>() {
            @Override
            public void onSuccess(List<FooParcelable> value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
    }

    public void testListParcelableDefault() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        List<FooParcelable> list1 = Hawk.get("tag", list);

        assertNotNull(list1);
    }

    public void testListParcelableDefaultCallback() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        Hawk.get("tag", list, new Hawk.Callback<List<FooParcelable>>() {
            @Override
            public void onSuccess(List<FooParcelable> value) {
                assertNotNull(value);
            }

            @Override
            public void onFail(Exception e) {
                fail("Could not get value correctly");
            }
        });
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

    public void testNullKeyGetCallback() {
        Hawk.get(null, new Hawk.Callback() {
            @Override
            public void onSuccess(Object o) {
                assertTrue(false);
            }

            @Override
            public void onFail(Exception e) {
                assertTrue(true);
            }
        });
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
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
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
