package com.orhanobut.hawk;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  private EncryptionMethod encryptionMethod;
  private String password;
  private LogLevel logLevel;
  private Storage cryptoStorage;
  private Encoder encoder;
  private Parser parser;
  private Encryption encryption;

  public enum EncryptionMethod {
    HIGHEST, MEDIUM, NO_ENCRYPTION
  }

  public HawkBuilder(Context context) {
    HawkUtils.checkNull("Context", context);

    this.context = context.getApplicationContext();
  }

  public HawkBuilder setEncryptionMethod(EncryptionMethod encryptionMethod) {
    this.encryptionMethod = encryptionMethod;
    return this;
  }

  public HawkBuilder setPassword(String password) {
    if (TextUtils.isEmpty(password)) {
      throw new NullPointerException("Password should not be null or empty");
    }
    this.password = password;
    return this;
  }

  public HawkBuilder setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
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

  HawkBuilder setEncoder(Encoder encoder) {
    this.encoder = encoder;
    return this;
  }

  HawkBuilder setEncryption(Encryption encryption) {
    this.encryption = encryption;
    return this;
  }

  EncryptionMethod getEncryptionMethod() {
    if (encryptionMethod == null) {
      encryptionMethod = EncryptionMethod.MEDIUM;
    }
    return encryptionMethod;
  }

  String getPassword() {
    return password;
  }

  LogLevel getLogLevel() {
    if (logLevel == null) {
      logLevel = LogLevel.NONE;
    }
    return logLevel;
  }

  Storage getStorage() {
    if (cryptoStorage == null) {
      cryptoStorage = new SharedPreferencesStorage(context, TAG);
    }
    return cryptoStorage;
  }

  Encoder getEncoder() {
    if (encoder == null) {
      encoder = new HawkEncoder(getParser());
    }
    return encoder;
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

  private void validate() {
    if (getEncryptionMethod() == EncryptionMethod.HIGHEST) {
      if (TextUtils.isEmpty(getPassword())) {
        throw new IllegalStateException("Password cannot be null " +
            "if encryption mode is highest");
      }
    }
  }

  public void build() {
    startBuild();
  }

  public void build(final Callback callback) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(new Runnable() {
      @Override public void run() {
        try {
          startBuild();
          callback.onSuccess();
        } catch (Exception e) {
          callback.onFail(e);
        }
      }
    });
    executor.shutdown();
  }

  void startBuild() {
    validate();
    setEncryption();
    Hawk.build(this);
  }

  private void setEncryption() {
    switch (getEncryptionMethod()) {
      case NO_ENCRYPTION:
        encryption = new Base64Encryption();
        break;
      case HIGHEST:
        encryption = new AesEncryption(getStorage(), getPassword());
        if (!getEncryption().init()) {
          getInfoStorage().put(KEY_NO_CRYPTO, true);
          encryption = new Base64Encryption();
        }
        break;
      case MEDIUM:
        encryption = new AesEncryption(getStorage(), null);
        if (!getEncryption().init()) {
          //fallback to no encryption
          getInfoStorage().put(KEY_NO_CRYPTO, true);
          encryption = new Base64Encryption();
        }
        break;
      default:
        throw new IllegalStateException("encryption mode should be valid");
    }
  }

  public static Storage newSharedPrefStorage(Context context) {
    return new SharedPreferencesStorage(context, TAG);
  }

  public interface Callback {

    void onSuccess();

    void onFail(Exception e);

  }
}
