package com.orhanobut.hawk;

import android.content.Context;


public final class Hawk {

  private Hawk() {
    // no instance
  }

  static HawkFacade hawkFacade = new HawkFacade.EmptyHawkFacade();

  /**
   * This will init the hawk without password protection.
   *
   * @param context is used to instantiate context based objects.
   *                ApplicationContext will be used
   */
  public static HawkBuilder init(Context context) {
    HawkUtils.checkNull("Context", context);
    hawkFacade = null;
    return new HawkBuilder(context);
  }

  static void build(HawkBuilder hawkBuilder) {
    hawkFacade = new DefaultHawkFacade(hawkBuilder);
  }

  /**
   * Saves any type including any collection, primitive values or custom objects
   *
   * @param key   is required to differentiate the given data
   * @param value is the data that is going to be encrypted and persisted
   *
   * @return true if the operation is successful. Any failure in any step will return false
   */
  public static <T> boolean put(String key, T value) {
    return hawkFacade.put(key, value);
  }

  /**
   * Gets the original data along with original type by the given key.
   * This is not guaranteed operation since Hawk uses serialization. Any change in in the requested
   * data type might affect the result. It's guaranteed to return primitive types and String type
   *
   * @param key is used to get the persisted data
   *
   * @return the original object
   */
  public static <T> T get(String key) {
    return hawkFacade.get(key);
  }

  /**
   * Gets the saved data, if it is null, default value will be returned
   *
   * @param key          is used to get the saved data
   * @param defaultValue will be return if the response is null
   *
   * @return the saved object
   */
  public static <T> T get(String key, T defaultValue) {
    return hawkFacade.get(key, defaultValue);
  }

  /**
   * Size of the saved data. Each key will be counted as 1
   *
   * @return the size
   */
  public static long count() {
    return hawkFacade.count();
  }

  /**
   * Clears the storage, note that crypto data won't be deleted such as salt key etc.
   * Use resetCrypto in order to deleteAll crypto information
   *
   * @return true if deleteAll is successful
   */
  public static boolean deleteAll() {
    return hawkFacade.deleteAll();
  }

  /**
   * Removes the given key/value from the storage
   *
   * @param key is used for removing related data from storage
   *
   * @return true if delete is successful
   */
  public static boolean delete(String key) {
    return hawkFacade.delete(key);
  }

  /**
   * Checks the given key whether it exists or not
   *
   * @param key is the key to check
   *
   * @return true if it exists in the storage
   */
  public static boolean contains(String key) {
    return hawkFacade.contains(key);
  }

  /**
   * Use this method to verify if Hawk is ready to be used.
   *
   * @return true if correctly initialised and built. False otherwise.
   */
  public static boolean isBuilt() {
    return hawkFacade.isBuilt();
  }

  public static void destroy() {
    hawkFacade.destroy();
  }

}
