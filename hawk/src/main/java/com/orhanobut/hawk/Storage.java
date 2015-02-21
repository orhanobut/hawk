package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
interface Storage {

    <T> void put(String key, T value);

    <T> T get(String key);

    void remove(String key);

    void clear();

    int count();

    boolean contains(String key);

}
