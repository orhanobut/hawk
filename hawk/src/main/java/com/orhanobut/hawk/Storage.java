package com.orhanobut.hawk;

import android.util.Pair;

import java.util.List;

public interface Storage {

  /**
   * Put a single entry to storage
   *
   * @param key   the name of entry to put
   * @param value the value of entry
   * @param <T>   type of value of entry
   * @return true if entry added successfully, otherwise false
   */
  <T> boolean put(String key, T value);

  /**
   * Put set of entries to storage
   *
   * @param items list of key&value entry pairs
   * @return true if all items added successfully, otherwise false
   */
  boolean put(List<Pair<String, ?>> items);

  /**
   * Get single entry from storage
   *
   * @param key the name of entry to get
   * @param <T> type of value of entry
   * @return the object related to given key
   */
  <T> T get(String key);

  /**
   * Remove single entry from storage
   *
   * @param key the name of entry to remove
   * @return true if removal is successful, otherwise false
   */
  boolean remove(String key);

  /**
   * Remove set of entries from storage
   *
   * @param keys the names of entries to remove
   * @return true if all removals are successful, otherwise false
   */
  boolean remove(String... keys);

  /**
   * Remove all entries in the storage
   *
   * @return true if clearance if successful, otherwise false
   */
  boolean clear();

  /**
   * Retrieve count of entries in the storage
   *
   * @return entry count in the storage
   */
  long count();

  /**
   * Checks whether the storage contains an entry.
   *
   * @param key the name of entry to check
   * @return true if the entry exists in the storage, otherwise false
   */
  boolean contains(String key);

}
