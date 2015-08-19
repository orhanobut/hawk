package com.orhanobut.hawksample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SharedPreferences.Editor editor;
  private SharedPreferences prefs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //    long start = System.nanoTime();
    //    Hawk.initWithoutEncryption(this, LogLevel.FULL);
    //    long end = System.nanoTime();
    //
    //    double resultHawk = getTime(start, end);
    //    log("Hawk init with password : " + resultHawk);
    //
    //    start = System.nanoTime();
    //    prefs = getSharedPreferences("BENCHARMK", MODE_PRIVATE);
    //    editor = prefs.edit();
    //    end = System.nanoTime();
    //    double resultPref = getTime(start, end);
    //    log("init: Prefs: " + resultPref + "    Hawk : " + resultHawk);
    //
    //
    //    benchmarkPrimitivePut();
    //    benchmarkStringPut();
    //    benchmarkListObjectPut();
    //    benchmarkListStringPut();
    //    benchmarkObjectPut();
    //    benchmarkMapPut();
    //    benchmarkSetPut();
    //
    //    benchmarkPrimitiveGet();
    //    benchmarkStringGet();
    //    benchmarkListObjectGet();
    //    benchmarkListStringGet();
    //    benchmarkObjectGet();
    //    benchmarkMapGet();
    //    benchmarkSetGet();
    //
    //    benchmarkDelete();

    Hawk.init(this)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
        .setPassword("password")
        .setStorage(HawkBuilder.newSharedPrefStorage(this))
        .setLogLevel(LogLevel.FULL)
        .setCallback(new HawkBuilder.Callback() {
          @Override
          public void onSuccess() {
            testRx();

            Hawk.put("joda", new DateTime());
            DateTime dateTime = Hawk.get("joda");
          }

          @Override
          public void onFail(Exception e) {

          }
        })
        .build();
  }

  private void testRx() {
    Hawk.putObservable(KEY, new Foo())
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Boolean>() {
          @Override
          public void onCompleted() {
          }

          @Override
          public void onError(Throwable e) {
          }

          @Override
          public void onNext(Boolean s) {
          }
        });

    Hawk.<Foo>getObservable(KEY)
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Foo>() {
          @Override
          public void onCompleted() {
            Log.d("rxtest", "completed");
          }

          @Override
          public void onError(Throwable e) {
            Log.d("rxtest", "error");
          }

          @Override
          public void onNext(Foo s) {
            Log.d("rxtest", s.toString());
          }
        });

  }

  private void testHawkInitWithoutPassword() {
    Hawk.init(this);
    Hawk.put("test123", "test");
    Hawk.init(this);
    Hawk.get("test123");
  }

  private static final String KEY = "KEY";

  private void benchmarkPrimitivePut() {
    int value = 13423;

    editor.clear().commit();
    long start = System.nanoTime();
    editor.putInt(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, value);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("int put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkStringPut() {
    String value = "something";

    editor.clear().commit();
    long start = System.nanoTime();
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, value);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("String put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkObjectPut() {
    Foo foo = new Foo();

    Gson gson = new Gson();
    editor.clear().commit();
    long start = System.nanoTime();
    String value = gson.toJson(foo);
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, foo);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("T put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkListStringPut() {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add("asdfasdfdsf");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    long start = System.nanoTime();
    String value = gson.toJson(list);
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, list);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("List<String> put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkMapPut() {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < 100; i++) {
      map.put("key", "value");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    long start = System.nanoTime();
    String value = gson.toJson(map);
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, map);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("Map<String,String> put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkSetPut() {
    Set<String> set = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      set.add("key");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    long start = System.nanoTime();
    String value = gson.toJson(set);
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, set);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("Set<String> put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkListObjectPut() {
    List<Foo> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(new Foo());
    }

    Gson gson = new Gson();
    editor.clear().commit();
    long start = System.nanoTime();
    String value = gson.toJson(list);
    editor.putString(KEY, value).commit();
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    start = System.nanoTime();
    Hawk.put(KEY, list);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("List<T> put : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkPrimitiveGet() {
    int value = 13423;

    editor.clear().commit();
    editor.putInt(KEY, value).commit();

    long start = System.nanoTime();
    prefs.getInt(KEY, 0);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, value);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("int get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkStringGet() {
    String value = "something";

    editor.clear().commit();
    editor.putString(KEY, value).commit();

    long start = System.nanoTime();
    prefs.getString(KEY, null);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, value);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("String get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkListStringGet() {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add("asdfasdfdsf");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    String value = gson.toJson(list);
    editor.putString(KEY, value).commit();
    long start = System.nanoTime();
    String json = prefs.getString(KEY, null);
    Type type = new TypeToken<ArrayList<String>>() {
    }.getType();
    gson.fromJson(json, type);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, list);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("List<String> get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkMapGet() {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < 100; i++) {
      map.put("key", "value");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    String value = gson.toJson(map);
    editor.putString(KEY, value).commit();
    long start = System.nanoTime();
    String json = prefs.getString(KEY, null);
    Type type = new TypeToken<Map<String, String>>() {
    }.getType();
    gson.fromJson(json, type);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, map);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("Map<String,String> get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkSetGet() {
    Set<String> map = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      map.add("key");
    }

    Gson gson = new Gson();
    editor.clear().commit();
    String value = gson.toJson(map);
    editor.putString(KEY, value).commit();
    long start = System.nanoTime();
    String json = prefs.getString(KEY, null);
    Type type = new TypeToken<Set<String>>() {
    }.getType();
    gson.fromJson(json, type);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, map);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("Set<String> get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkListObjectGet() {
    List<Foo> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(new Foo());
    }

    Gson gson = new Gson();
    editor.clear().commit();
    String value = gson.toJson(list);
    editor.putString(KEY, value).commit();
    long start = System.nanoTime();
    String json = prefs.getString(KEY, null);
    Type type = new TypeToken<ArrayList<Foo>>() {
    }.getType();
    gson.fromJson(json, type);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, list);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("List<T> get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }


  private void benchmarkObjectGet() {
    Foo foo = new Foo();

    Gson gson = new Gson();
    editor.clear().commit();
    String value = gson.toJson(foo);
    editor.putString(KEY, value).commit();
    long start = System.nanoTime();
    String json = prefs.getString(KEY, null);
    gson.fromJson(json, Foo.class);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, foo);
    start = System.nanoTime();
    Hawk.get(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("Object get : Pref : " + resultPref + "  -- Hawk : " + resultHawk);

  }

  private void benchmarkDelete() {
    int value = 13423;

    editor.clear().commit();
    editor.putInt(KEY, value).commit();

    long start = System.nanoTime();
    editor.remove(KEY);
    long end = System.nanoTime();
    double resultPref = getTime(start, end);

    Hawk.clear();
    Hawk.put(KEY, value);
    start = System.nanoTime();
    Hawk.remove(KEY);
    end = System.nanoTime();
    double resultHawk = getTime(start, end);

    log("int remove : Pref : " + resultPref + "  -- Hawk : " + resultHawk);
  }


  private double getTime(long start, long end) {
    return (end - start) / 1e6d;
  }

  private void log(String message) {
    Log.d(TAG + " ---- BENCHMARK : ", message);
  }


}
