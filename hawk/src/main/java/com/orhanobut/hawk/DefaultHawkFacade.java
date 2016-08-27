package com.orhanobut.hawk;

public class DefaultHawkFacade implements HawkFacade {

  private final Storage storage;
  private final Converter converter;
  private final Encryption encryption;
  private final Serializer serializer;

  public DefaultHawkFacade(HawkBuilder builder) {
    encryption = builder.getEncryption();
    storage = builder.getStorage();
    converter = builder.getConverter();
    serializer = builder.getSerializer();
  }

  @Override public <T> boolean put(String key, T value) {
    // Validate
    HawkUtils.checkNull("Key", key);

    // If the value is null, delete it
    if (value == null) return delete(key);

    // 1. Convert to text
    String plainText = converter.toString(value);
    if (plainText == null) return false;

    // 2. Encrypt the text
    String cipherText = null;
    try {
      cipherText = encryption.encrypt(key, plainText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (cipherText == null) return false;

    // 3. Serialize the given object along with the cipher text
    String encodedText = serializer.serialize(cipherText, value);
    if (encodedText == null) return false;

    // 4. Save to the storage
    return storage.put(key, encodedText);
  }

  @Override public <T> T get(String key) {
    if (key == null) return null;

    // 1. Get serialized text from the storage
    String serializedText = storage.get(key);
    if (serializedText == null) return null;

    // 2. Deserialize
    DataInfo dataInfo = serializer.deserialize(serializedText);
    if (dataInfo == null) return null;

    // 3. Decrypt
    String plainText = null;
    try {
      plainText = encryption.decrypt(key, dataInfo.cipherText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (plainText == null) return null;

    // 4. Convert the text to original data along with original type
    try {
      return converter.fromString(plainText, dataInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Return null in any failure
    return null;
  }

  @Override public <T> T get(String key, T defaultValue) {
    T t = get(key);
    if (t == null) return defaultValue;
    return t;
  }

  @Override public long count() {
    return storage.count();
  }

  @Override public boolean deleteAll() {
    return storage.deleteAll();
  }

  @Override public boolean delete(String key) {
    return storage.delete(key);
  }

  @Override public boolean contains(String key) {
    return storage.contains(key);
  }

  @Override public boolean isBuilt() {
    return true;
  }

  @Override public void destroy() {
  }
}
