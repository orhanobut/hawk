package com.orhanobut.hawk;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SharedPreferencesStorageTest {

  SharedPreferencesStorage storage;
  SharedPreferences.Editor editor;
  SharedPreferences preferences;

  @Before public void setup() {
    editor = spy(new SharedPreferences.Editor() {
      @Override public SharedPreferences.Editor putString(String key, String value) {
        return this;
      }

      @Override public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
        return this;
      }

      @Override public SharedPreferences.Editor putInt(String key, int value) {
        return this;
      }

      @Override public SharedPreferences.Editor putLong(String key, long value) {
        return this;
      }

      @Override public SharedPreferences.Editor putFloat(String key, float value) {
        return this;
      }

      @Override public SharedPreferences.Editor putBoolean(String key, boolean value) {
        return this;
      }

      @Override public SharedPreferences.Editor remove(String key) {
        return this;
      }

      @Override public SharedPreferences.Editor clear() {
        return this;
      }

      @Override public boolean commit() {
        return true;
      }

      @Override public void apply() {
      }
    });
    preferences = spy(new SharedPreferences() {
      @Override public Map<String, ?> getAll() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        return map;
      }

      @Override public String getString(String key, String defValue) {
        return null;
      }

      @Override public Set<String> getStringSet(String key, Set<String> defValues) {
        return null;
      }

      @Override public int getInt(String key, int defValue) {
        return 0;
      }

      @Override public long getLong(String key, long defValue) {
        return 0;
      }

      @Override public float getFloat(String key, float defValue) {
        return 0;
      }

      @Override public boolean getBoolean(String key, boolean defValue) {
        return false;
      }

      @Override public boolean contains(String key) {
        return false;
      }

      @Override public Editor edit() {
        return editor;
      }

      @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

      }

      @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

      }
    });
    storage = new SharedPreferencesStorage(preferences);
  }

  @Test public void putObject() throws Exception {
    storage.put("key", "value");

    verify(preferences).edit();
    verify(editor).putString("key", "value");
    verify(editor).commit();
  }

  @Test public void throwExceptionOnNullKeysPassedOnPut() {
    try {
      storage.put(null, "value");
      fail("key should not be null");
    } catch (Exception e) {
      assertThat(e).hasMessage("key should not be null");
    }
  }

  @Test public void get() throws Exception {
    storage.get("key");

    verify(preferences).getString("key", null);
  }

  @Test public void remove() throws Exception {
    storage.delete("key");

    verify(preferences).edit();
    verify(editor).remove("key");
    verify(editor).commit();
  }

  @Test public void contains() throws Exception {
    storage.contains("key");

    verify(preferences).contains("key");
  }

  @Test public void clear() throws Exception {
    storage.clear();

    verify(preferences).edit();
    verify(editor).clear();
    verify(editor).commit();
  }

  @Test public void count() throws Exception {
    long count = storage.count();

    verify(preferences).getAll();
    assertThat(count).isEqualTo(1);
  }
}