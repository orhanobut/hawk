package com.orhanobut.hawk;

import android.util.Pair;

import java.util.List;

/**
 * @author Orhan Obut
 */
public class SqliteStorage implements Storage {

  @Override
  public <T> boolean put(String key, T value) {
    return false;
  }

  @Override
  public boolean put(List<Pair<String, ?>> items) {
    return false;
  }

  @Override
  public <T> T get(String key) {
    return null;
  }

  @Override
  public boolean remove(String key) {
    return false;
  }

  @Override
  public boolean remove(String... keys) {
    return false;
  }

  @Override
  public boolean clear() {
    return false;
  }

  @Override
  public int count() {
    return 0;
  }

  @Override
  public boolean contains(String key) {
    return false;
  }
}
