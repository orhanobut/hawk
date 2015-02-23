package com.orhanobut.hawk.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import java.io.Serializable;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Hawk.init(this);
        Hawk.clear();

        test();
        test2();
    }

    void test() {
        Integer a = 5;
        Hawk.put(TAG, a);

        int b = Hawk.get(TAG);
        Log.d(TAG, String.valueOf(b));
    }

    void test2() {
        Foo foo = new Foo();
        Hawk.put("test2", foo);
        Foo foo1 = Hawk.get("test2");
    }

    static class Foo implements Serializable {
        String a = "foo";
    }

}
