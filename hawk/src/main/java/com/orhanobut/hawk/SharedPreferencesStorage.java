package com.orhanobut.hawk;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class SharedPreferencesStorage implements Storage {

  private final SharedPreferences preferences;

  SharedPreferencesStorage(Context context, String tag) {
    preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
  }

  SharedPreferencesStorage(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  @Override public <T> boolean put(String key, T value) {
    HawkUtils.checkNull("key", key);
    return getEditor().putString(key, String.valueOf(value)).commit();
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T get(String key) {
    return (T) preferences.getString(key, null);
  }

  @Override public boolean delete(String key) {
    return getEditor().remove(key).commit();
  }

  @Override public boolean contains(String key) {
    return preferences.contains(key);
  }

  @Override public boolean deleteAll() {
    return getEditor().clear().commit();
  }

  @Override public long count() {
    return preferences.getAll().size();
  }

  private SharedPreferences.Editor getEditor() {
    return preferences.edit();
  }

  @Override
  public List<String> getAllKeys() {
    List<String> result = new ArrayList<>();
    Map<String, ?> allEntries = preferences.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      result.add(entry.getKey());
    }
    return result;
  }

  @Override
  public long selectiveDelete(List<String> keys) {
    long result = 0;
    Map<String, ?> allEntries = preferences.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      if (!keys.contains(entry.getKey())) {
        delete(entry.getKey());
        result++;
      }
    }
    return result;
  }
}
