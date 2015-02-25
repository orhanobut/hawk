package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
interface Storage {

    <T> boolean put(String key, T value);

    <T> T get(String key);

    boolean remove(String key);

    boolean clear();

    int count();

    boolean contains(String key);

}
