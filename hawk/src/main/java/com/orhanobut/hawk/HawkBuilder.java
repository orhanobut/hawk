package com.orhanobut.hawk;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

public class HawkBuilder {

  /**
   * NEVER ever change TAG and TAG_INFO.
   * It will break backward compatibility in terms of keeping previous data
   */
  private static final String TAG = "HAWK";
  private static final String TAG_INFO = "324909sdfsd98098";

  /**
   * Key to store if the device does not support crypto.
   * This key has nothing to do with encryption key,
   * This is merely used for the key tag for preferences value.
   */
  private static final String KEY_NO_CRYPTO = "dfsklj2342nasdfoasdfcrpknasdf";

  private Context context;
  private String password;
  private Storage cryptoStorage;
  private Converter converter;
  private Parser parser;
  private Encryption encryption;

  public enum EncryptionMethod {
    HIGHEST, MEDIUM, NO_ENCRYPTION
  }

  public HawkBuilder(Context context) {
    HawkUtils.checkNull("Context", context);

    this.context = context.getApplicationContext();
  }

  public HawkBuilder setPassword(String password) {
    if (TextUtils.isEmpty(password)) {
      throw new NullPointerException("Password should not be null or empty");
    }
    this.password = password;
    return this;
  }

  public HawkBuilder setStorage(Storage storage) {
    this.cryptoStorage = storage;
    return this;
  }

  public HawkBuilder setParser(Parser parser) {
    this.parser = parser;
    return this;
  }

  HawkBuilder setConverter(Converter converter) {
    this.converter = converter;
    return this;
  }

  HawkBuilder setEncryption(Encryption encryption) {
    this.encryption = encryption;
    return this;
  }

  String getPassword() {
    return password;
  }

  Storage getStorage() {
    if (cryptoStorage == null) {
      cryptoStorage = new SharedPreferencesStorage(context, TAG);
    }
    return cryptoStorage;
  }

  Converter getConverter() {
    if (converter == null) {
      converter = new HawkConverter(getParser());
    }
    return converter;
  }

  Storage getInfoStorage() {
    return new SharedPreferencesStorage(context, TAG_INFO);
  }

  Parser getParser() {
    if (parser == null) {
      parser = new GsonParser(new Gson());
    }
    return parser;
  }

  Encryption getEncryption() {
    return encryption;
  }

  public void build() {
    startBuild();
  }

  void startBuild() {
    setEncryption();
    Hawk.build(this);
  }

  private void setEncryption() {
    encryption = new ConcealEncryption(context);
    if (!getEncryption().init()) {
      getInfoStorage().put(KEY_NO_CRYPTO, true);
      encryption = new Base64Encryption();
    }
  }

  public static Storage newSharedPrefStorage(Context context) {
    return new SharedPreferencesStorage(context, TAG);
  }
}
