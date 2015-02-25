package com.orhanobut.hawk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Orhan Obut
 */
final class SharedPreferencesStorage implements Storage {

    private final SharedPreferences preferences;

    SharedPreferencesStorage(Context context, String tag) {
        preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
    }

    @Override
    public <T> void put(String key, T value) {
        getEditor().putString(key, String.valueOf(value)).commit();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) preferences.getString(key, null);
    }

    @Override
    public void remove(String key) {
        getEditor().remove(key).commit();
    }

    @Override
    public boolean contains(String key) {
        return preferences.contains(key);
    }

    @Override
    public void clear() {
        getEditor().clear().commit();
    }

    @Override
    public int count() {
        return preferences.getAll().size();
    }

    private SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

}
