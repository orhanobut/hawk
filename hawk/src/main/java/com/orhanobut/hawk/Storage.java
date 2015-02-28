package com.orhanobut.hawk;

import android.util.Pair;

import java.util.List;

/**
 * @author Orhan Obut
 */
interface Storage {

    <T> boolean put(String key, T value);

    void put(List<Pair<String, ?>> items);

    <T> T get(String key);

    boolean remove(String key);

    boolean clear();

    int count();

    boolean contains(String key);

}
