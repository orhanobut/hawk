package com.orhanobut.hawk;

import android.util.Pair;

import java.util.List;
import java.util.Map;

public abstract class Storage {

  private List<OnDataChangedListener> dataChangedListeners;

  /**
   * Put a single entry to storage
   *
   * @param key   the name of entry to put
   * @param value the value of entry
   * @param <T>   type of value of entry
   * @return true if entry added successfully, otherwise false
   */
  public abstract <T> boolean put(String key, T value);

  /**
   * Put set of entries to storage
   *
   * @param items list of key&value entry pairs
   * @return true if all items added successfully, otherwise false
   */
  public abstract boolean put(List<Pair<String, ?>> items);

  /**
   * Get single entry from storage
   *
   * @param key the name of entry to get
   * @param <T> type of value of entry
   * @return the object related to given key
   */
  public abstract <T> T get(String key);

  /**
   * Get all entries from storage
   *
   * @return a map with all objects that are currently in the storage
   */
  public abstract Map<String, ?> getAll();

  /**
   * Remove single entry from storage
   *
   * @param key the name of entry to remove
   * @return true if removal is successful, otherwise false
   */
  public abstract boolean remove(String key);

  /**
   * Remove set of entries from storage
   *
   * @param keys the names of entries to remove
   * @return true if all removals are successful, otherwise false
   */
  public abstract boolean remove(String... keys);

  /**
   * Remove all entries in the storage
   *
   * @return true if clearance if successful, otherwise false
   */
  public abstract boolean clear();

  /**
   * Retrieve count of entries in the storage
   *
   * @return entry count in the storage
   */
  public abstract long count();

  /**
   * Checks whether the storage contains an entry.
   *
   * @param key the name of entry to check
   * @return true if the entry exists in the storage, otherwise false
   */
  public abstract boolean contains(String key);

  public void registerOnDataChangedListener(OnDataChangedListener listener) {
    if (dataChangedListeners != null && listener != null && !dataChangedListeners.contains(listener)) {
      dataChangedListeners.add(listener);
    }
  }

  public void unregisterOnDataChangedListener(OnDataChangedListener listener) {
    if (listener != null && dataChangedListeners != null) {
      dataChangedListeners.remove(listener);
    }
  }

  public void notifyOnDataChangedListeners(String key) {
        if (dataChangedListeners != null) {
      for (OnDataChangedListener listener : dataChangedListeners) {
        listener.onDataChanged(key);
      }
    }
  }

}
