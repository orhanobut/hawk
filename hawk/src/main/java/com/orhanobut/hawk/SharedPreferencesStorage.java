package com.orhanobut.hawk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Orhan Obut
 */
final class SharedPreferencesStorage implements Storage {

    private final Context context;
    private final String tag;

    SharedPreferencesStorage(Context context, String tag) {
        this.context = context;
        this.tag = tag;
    }

    @Override
    public <T> void put(String key, T value) {
        getEditor().putString(key, String.valueOf(value)).commit();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) getSharedPreferences().getString(key, null);
    }

    @Override
    public void clear() {
        getEditor().clear().commit();
    }

    @Override
    public int count() {
        return getSharedPreferences().getAll().size();
    }

    private SharedPreferences.Editor getEditor() {
        SharedPreferences preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        return preferences.edit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(tag, Context.MODE_PRIVATE);
    }
}
