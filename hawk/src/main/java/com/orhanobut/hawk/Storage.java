package com.orhanobut.hawk;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public interface Storage {

  /**
   * Put a single entry to storage
   *
   * @param key   the name of entry to put
   * @param value the value of entry
   * @param <T>   type of value of entry
   *
   * @return true if entry added successfully, otherwise false
   */
  <T> boolean put(String key, T value);

  /**
   * Get single entry from storage
   *
   * @param key the name of entry to get
   * @param <T> type of value of entry
   *
   * @return the object related to given key
   */
  <T> T get(String key);

  /**
   * Remove single entry from storage
   *
   * @param key the name of entry to delete
   *
   * @return true if removal is successful, otherwise false
   */
  boolean delete(String key);

  /**
   * Remove all entries in the storage
   *
   * @return true if clearance if successful, otherwise false
   */
  boolean deleteAll();

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
   *
   * @return true if the entry exists in the storage, otherwise false
   */
  boolean contains(String key);

  /**
   * Retrieve a list with all the keys in the storage
   *
   * @return list of keys
   */
  List<String> getAllKeys();
}
