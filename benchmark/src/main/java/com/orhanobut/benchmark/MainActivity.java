package com.orhanobut.benchmark;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.LogInterceptor;

import java.util.Arrays;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    timeHawkInit();
    timeHawkPut();
    timeHawkGet();
    timeHawkContains();
    timeHawkCount();
    timeHawkGetKeys();
    timeHawkDelete();
    timeHawkSelectiveDelete();
  }

  private void timeHawkInit() {
    long startTime = System.currentTimeMillis();

    Hawk.init(this).setLogInterceptor(new LogInterceptor() {
      @Override public void onLog(String message) {
        Log.d("HAWK", message);
      }
    }).build();

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.init: " + (endTime - startTime) + "ms");
  }

  private void timeHawkPut() {
    long startTime = System.currentTimeMillis();

    Hawk.put("key", "value");

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.put: " + (endTime - startTime) + "ms");
  }

  private void timeHawkGet() {
    long startTime = System.currentTimeMillis();

    Hawk.get("key");

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.get: " + (endTime - startTime) + "ms");
  }

  private void timeHawkCount() {
    long startTime = System.currentTimeMillis();

    Hawk.count();

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.count: " + (endTime - startTime) + "ms");
  }

  private void timeHawkContains() {
    long startTime = System.currentTimeMillis();

    Hawk.contains("key");

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.count: " + (endTime - startTime) + "ms");
  }

  private void timeHawkDelete() {
    long startTime = System.currentTimeMillis();

    Hawk.delete("key");

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.count: " + (endTime - startTime) + "ms");
  }

  private void timeHawkGetKeys() {
    long startTime = System.currentTimeMillis();

    Hawk.getAllKeys();

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.getAllKeys: " + (endTime - startTime) + "ms");
  }

  private void timeHawkSelectiveDelete() {
    long startTime = System.currentTimeMillis();

    Hawk.selectiveDelete(Arrays.asList("key", "key2", "key3"));

    long endTime = System.currentTimeMillis();
    System.out.println("Hawk.getAllKeys: " + (endTime - startTime) + "ms");
  }
}
