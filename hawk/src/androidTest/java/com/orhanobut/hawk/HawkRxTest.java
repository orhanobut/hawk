package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Orhan Obut
 */
public class HawkRxTest extends InstrumentationTestCase {

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
    Hawk.resetCrypto();
  }

  private static final String KEY = "TAG";

  public void testString() throws Exception {
    Hawk.put(KEY, "hawk");

    Hawk.<String>getObservable(KEY)
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<String>() {
          @Override
          public void onCompleted() {
            Log.d("rxtest", "completed");
            assertTrue(true);
          }

          @Override
          public void onError(Throwable e) {
            Log.d("rxtest", "error");
            assertTrue(false);
          }

          @Override
          public void onNext(String s) {
            Log.d("rxtest", s);
            assertTrue(true);
          }
        });
  }

}
