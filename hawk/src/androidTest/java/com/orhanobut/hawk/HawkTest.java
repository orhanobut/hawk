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
        Hawk.init(context);
        Hawk.clear();
        Hawk.resetCrypto();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        context = null;
    }

    public void testBoolean() {
        boolean value = true;
        Hawk.put("tag", value);
        assertEquals(value, Hawk.get("tag"));
    }

    public void testChar() {
        char expected = 'a';
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testByte() {
        byte expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testShort() {
        short expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testInt() {
        int expected = 0;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testLong() {
        long expected = 100L;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testFloat() {
        float expected = 0.1f;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testDouble() {
        double expected = 11;
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testString() {
        String expected = "test";
        Hawk.put("tag", expected);
        assertEquals(expected, Hawk.get("tag"));
    }

    public void testSerializableObject() {
        FooSerializable foo = new FooSerializable();

        Hawk.put("tag", foo);
        FooSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testNotSerializableObject() {
        FooNotSerializable foo = new FooNotSerializable();

        Hawk.put("tag", foo);
        FooNotSerializable foo1 = Hawk.get("tag");

        assertNotNull(foo1);
    }

    public void testParcelableObject() {
        FooParcelable foo = new FooParcelable();
        Hawk.put("tag", foo);
        FooParcelable fooParcelable = Hawk.get("tag");

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

    public void testListParcelable() {
        List<FooParcelable> list = new ArrayList<>();
        list.add(new FooParcelable());
        list.add(new FooParcelable());

        Hawk.put("tag", list);

        List<FooParcelable> list1 = Hawk.get("tag");

        assertNotNull(list1);
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

}
