package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * @author Orhan Obut
 */
public class HawkCallbackTest extends InstrumentationTestCase {

    Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(
                "dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());
        context = getInstrumentation().getContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        context = null;
        Hawk.clear();
        Hawk.resetCrypto();
    }

    public void testInitCallback() {
        //sleep time to be executed while waiting callback functions
        final int SLEEP_TIME = 1500;
        Hawk.init(context, "testPassword", LogLevel.FULL, new Hawk.Callback() {
            @Override
            public void onSuccess() {
                Hawk.put("key", "value");
                assertEquals("value", Hawk.get("key"));
            }

            @Override
            public void onFail(Exception e) {
                fail("Init with callback failed");
            }
        });
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void testInitCallbackWithoutLogLevel() {
        //sleep time to be executed while waiting callback functions
        final int SLEEP_TIME = 1500;
        Hawk.init(context, "testPassword", new Hawk.Callback() {
            @Override
            public void onSuccess() {
                Hawk.put("key", "value");
                assertEquals("value", Hawk.get("key"));
            }

            @Override
            public void onFail(Exception e) {
                fail("Init with callback failed");
            }
        });
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
