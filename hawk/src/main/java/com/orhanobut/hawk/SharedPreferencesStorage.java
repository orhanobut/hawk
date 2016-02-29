package com.orhanobut.hawk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

final class SharedPreferencesStorage implements Storage {

	private final SharedPreferences preferences;

	private final List<HawkPreferenceChangedListener> sharedPrefsListener;

	SharedPreferencesStorage(Context context, String tag) {
		preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		sharedPrefsListener = new ArrayList<>();
	}

	SharedPreferencesStorage(SharedPreferences preferences) {
		this.preferences = preferences;
		sharedPrefsListener = new ArrayList<>();
	}

	@Override
	public <T> boolean put(String key, T value) {
		HawkUtils.checkNull("key", key);
		boolean success = getEditor().putString(key, String.valueOf(value)).commit();

		notifyAllHawkPrefsChangedListener(key);

		return success;
	}

	@Override
	public boolean put(List<Pair<String, ?>> items) {
		SharedPreferences.Editor editor = getEditor();
		for (Pair<String, ?> item : items) {
			editor.putString(item.first, String.valueOf(item.second));
			notifyAllHawkPrefsChangedListener(item.first);
		}

		return editor.commit();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key) {
		return (T) preferences.getString(key, null);
	}

	@Override
	public boolean remove(String key) {
		boolean success = getEditor().remove(key).commit();

		notifyAllHawkPrefsChangedListener(key);

		return success;
	}

	@Override
	public boolean remove(String... keys) {
		SharedPreferences.Editor editor = getEditor();
		for (String key : keys) {
			editor.remove(key);
			notifyAllHawkPrefsChangedListener(key);
		}

		return editor.commit();
	}

	@Override
	public boolean contains(String key) {
		return preferences.contains(key);
	}

	@Override
	public boolean clear() {
		return getEditor().clear().commit();
	}

	@Override
	public long count() {
		return preferences.getAll().size();
	}

	private SharedPreferences.Editor getEditor() {
		return preferences.edit();
	}

	public void registerHawkPrefsChangedListener(HawkPreferenceChangedListener listener) {
		if (sharedPrefsListener != null && listener != null && !sharedPrefsListener.contains(listener)) {
			sharedPrefsListener.add(listener);
		}
	}

	public void unregisterHawkPrefsChangedListener(HawkPreferenceChangedListener listener) {
		if (listener != null) {
			sharedPrefsListener.remove(listener);
		}
	}

	private void notifyAllHawkPrefsChangedListener(String key) {
		if (sharedPrefsListener != null) {
			for (HawkPreferenceChangedListener listener : sharedPrefsListener) {
				listener.onHawkPreferenceChanged(key);
			}
		}
	}

}
