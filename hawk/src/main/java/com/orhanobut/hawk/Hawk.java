package com.orhanobut.hawk;

import android.content.Context;


public final class Hawk {

  static Hawk HAWK;

  private final Storage storage;
  private final Encoder encoder;
  private final Encryption encryption;
  private final LogLevel logLevel;

  private Hawk(HawkBuilder builder) {
    storage = builder.getStorage();
    encoder = builder.getEncoder();
    encryption = builder.getEncryption();
    logLevel = builder.getLogLevel();
  }

  /**
   * This will init the hawk without password protection.
   *
   * @param context is used to instantiate context based objects.
   *                ApplicationContext will be used
   */
  public static HawkBuilder init(Context context) {
    HawkUtils.checkNull("Context", context);
    HAWK = null;
    return new HawkBuilder(context);
  }

  static void build(HawkBuilder hawkBuilder) {
    HAWK = new Hawk(hawkBuilder);
  }

  /**
   * Saves every type of Objects. List, List<T>, primitives
   *
   * @param key   is used to save the data
   * @param value is the data that is gonna be saved. Value can be object, list type, primitives
   *
   * @return true if put is successful
   */
  public static <T> boolean put(String key, T value) {
    HawkUtils.checkNull("Key", key);
    HawkUtils.validateBuild();

    //if the value is null, simply delete it
    if (value == null) {
      return delete(key);
    }

    String encodedText = zip(value);
    //if any exception occurs during encoding, encodedText will be null and thus operation is unsuccessful
    return encodedText != null && HAWK.storage.put(key, encodedText);
  }

  /**
   * @param key is used to get the saved data
   *
   * @return the saved object
   */
  public static <T> T get(String key) {
    HawkUtils.checkNull("Key", key);
    HawkUtils.validateBuild();

    String fullText = HAWK.storage.get(key);
    if (fullText == null) {
      return null;
    }

    DataInfo dataInfo = DataHelper.getDataInfo(fullText);
    byte[] bytes = HAWK.encryption.decrypt(dataInfo.cipherText);

    if (bytes == null) {
      return null;
    }

    try {
      return HAWK.encoder.decode(bytes, dataInfo);
    } catch (Exception e) {
      Logger.d(e.getMessage());
    }
    return null;
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
    T t = get(key);
    if (t == null) {
      return defaultValue;
    }
    return t;
  }

  /**
   * Size of the saved data. Each key will be counted as 1
   *
   * @return the size
   */
  public static long count() {
    HawkUtils.validateBuild();
    return HAWK.storage.count();
  }

  /**
   * Clears the storage, note that crypto data won't be deleted such as salt key etc.
   * Use resetCrypto in order to clear crypto information
   *
   * @return true if clear is successful
   */
  public static boolean clear() {
    HawkUtils.validateBuild();
    return HAWK.storage.clear();
  }

  /**
   * Removes the given key/value from the storage
   *
   * @param key is used for removing related data from storage
   *
   * @return true if delete is successful
   */
  public static boolean delete(String key) {
    HawkUtils.validateBuild();
    return HAWK.storage.delete(key);
  }

  /**
   * Checks the given key whether it exists or not
   *
   * @param key is the key to check
   *
   * @return true if it exists in the storage
   */
  public static boolean contains(String key) {
    HawkUtils.validateBuild();
    return HAWK.storage.contains(key);
  }

  /**
   * Clears all saved data that is used for the crypto
   *
   * @return true if reset is successful
   */
  public static boolean resetCrypto() {
    HawkUtils.validateBuild();
    return HAWK.encryption.reset();
  }

  public static LogLevel getLogLevel() {
    if (HAWK == null) {
      return LogLevel.NONE;
    }
    return HAWK.logLevel;
  }

  /**
   * Use this method to verify if Hawk is ready to be used.
   *
   * @return true if correctly initialised and built. False otherwise.
   */
  public static boolean isBuilt() {
    return HAWK != null;
  }

  /**
   * Encodes the given value as full text (cipher + data info)
   *
   * @param value is the given value to encode
   *
   * @return full text as string
   */
  private static <T> String zip(T value) {
    HawkUtils.checkNull("Value", value);

    byte[] encodedValue = HAWK.encoder.encode(value);

    if (encodedValue == null || encodedValue.length == 0) {
      return null;
    }

    String cipherText = HAWK.encryption.encrypt(encodedValue);

    if (cipherText == null) {
      return null;
    }
    return DataHelper.addType(cipherText, value);
  }

  public static void destroy() {
    HAWK = null;
  }

}
