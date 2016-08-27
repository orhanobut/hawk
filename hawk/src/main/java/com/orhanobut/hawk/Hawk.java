package com.orhanobut.hawk;

import android.content.Context;


public final class Hawk {

  static Hawk HAWK;

  private final Storage storage;
  private final Converter converter;
  private final Encryption encryption;
  private final Serializer serializer;

  private Hawk(HawkBuilder builder) {
    storage = builder.getStorage();
    converter = builder.getConverter();
    encryption = builder.getEncryption();
    serializer = new HawkSerializer();
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
   * Saves any type including any collection, primitive values or custom objects
   *
   * @param key   is required to differentiate the given data
   * @param value is the data that is going to be encrypted and persisted
   *
   * @return true if the operation is successful. Any failure in any step will return false
   */
  public static <T> boolean put(String key, T value) {
    // Validate
    HawkUtils.validateBuild();
    HawkUtils.checkNull("Key", key);

    // If the value is null, delete it
    if (value == null) return delete(key);

    // 1. Convert to text
    String plainText = HAWK.converter.toString(value);
    if (plainText == null) return false;

    // 2. Encrypt the text
    String cipherText = null;
    try {
      cipherText = HAWK.encryption.encrypt(key, plainText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (cipherText == null) return false;

    // 3. Serialize the given object along with the cipher text
    String encodedText = HAWK.serializer.serialize(cipherText, value);
    if (encodedText == null) return false;

    // 4. Save to the storage
    return HAWK.storage.put(key, encodedText);
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

    // validate
    HawkUtils.checkNull("Key", key);
    HawkUtils.validateBuild();

    // 1. Get serialized text from the storage
    String serializedText = HAWK.storage.get(key);
    if (serializedText == null) return null;

    // 2. Deserialize
    DataInfo dataInfo = HAWK.serializer.deserialize(serializedText);
    if (dataInfo == null) return null;

    // 3. Decrypt
    String plainText = null;
    try {
      plainText = HAWK.encryption.decrypt(key, dataInfo.cipherText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (plainText == null) return null;

    // 4. Convert the text to original data along with original type
    try {
      return HAWK.converter.fromString(plainText, dataInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Return null in any failure
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
    if (t == null) return defaultValue;
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
   * Use resetCrypto in order to deleteAll crypto information
   *
   * @return true if deleteAll is successful
   */
  public static boolean deleteAll() {
    HawkUtils.validateBuild();
    return HAWK.storage.deleteAll();
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

  /**
   * Use this method to verify if Hawk is ready to be used.
   *
   * @return true if correctly initialised and built. False otherwise.
   */
  public static boolean isBuilt() {
    return HAWK != null;
  }

  public static void destroy() {
    HAWK = null;
  }

}
