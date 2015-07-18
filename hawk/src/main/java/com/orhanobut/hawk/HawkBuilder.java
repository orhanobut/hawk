package com.orhanobut.hawk;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;

/**
 * @author Orhan Obut
 */
public class HawkBuilder {

  /**
   * never ever change this value since it will break backward compatibility in terms of keeping previous data
   */
  private static final String TAG = "HAWK";

  /**
   * never ever change this value since it will break backward compatibility in terms of keeping previous data
   */
  private static final String TAG_INFO = "324909sdfsd98098";

  /**
   * Key to store if the device does not support crypto
   */
  private static final String KEY_NO_CRYPTO = "dfsklj2342nasdfoasdfcrpknasdf";

  private Context context;
  private EncryptionMethod encryptionMethod;
  private String password;
  private LogLevel logLevel;
  private Storage cryptoStorage;
  private Storage infoStorage;
  private Encoder encoder;
  private Parser parser;
  private Encryption encryption;
  private Callback callback;
  private static ExecutorService executorService;

  public enum EncryptionMethod {
    HIGHEST, MEDIUM, NO_ENCRYPTION
  }

  public HawkBuilder(Context context) {
    this.context = context.getApplicationContext();
  }

  public HawkBuilder setEncryptionMethod(EncryptionMethod encryptionMethod) {
    this.encryptionMethod = encryptionMethod;
    return this;
  }

  public HawkBuilder setPassword(String password) {
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

  public HawkBuilder setCallback(Callback callback) {
    this.callback = callback;
    return this;
  }

  public Context getContext() {
    return context;
  }

  public EncryptionMethod getEncryptionMethod() {
    return encryptionMethod;
  }

  public String getPassword() {
    return password;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public Storage getStorage() {
    return cryptoStorage;
  }

  public Encoder getEncoder() {
    return encoder;
  }

  public Parser getParser() {
    return parser;
  }

  public Encryption getEncryption() {
    return encryption;
  }

  public boolean isEncrypted() {
    return encryptionMethod != EncryptionMethod.NO_ENCRYPTION;
  }

  private void init() {
    infoStorage = new SharedPreferencesStorage(context, TAG_INFO);
    if (logLevel == null) {
      logLevel = LogLevel.NONE;
    }
    if (parser == null) {
      parser = new GsonParser(new Gson());
    }
    if (encoder == null) {
      encoder = new HawkEncoder(parser);
    }
    if (encryptionMethod == null) {
      encryptionMethod = EncryptionMethod.MEDIUM;
    }
    if (cryptoStorage == null) {
      cryptoStorage = new SharedPreferencesStorage(context, TAG);
    }
  }

  private void validate() {
    if (encryptionMethod == EncryptionMethod.HIGHEST) {
      if (TextUtils.isEmpty(password)) {
        throw new IllegalStateException("Password cannot be null " +
            "if encryption mode is highest");
      }
    }
  }

  public void build() {
    if (callback != null) {
      new Handler().post(new Runnable() {
        @Override
        public void run() {
          try {
            startBuild();
            callback.onSuccess();
          } catch (Exception e) {
            callback.onFail(e);
          }
        }
      });
      return;
    }
    startBuild();
  }

  private void startBuild() {
    validate();
    init();
    setEncryption();
  }

  private void setEncryption() {
    switch (encryptionMethod) {
      case NO_ENCRYPTION:
        break;
      case HIGHEST:
        encryption = new AesEncryption(cryptoStorage, password);
        boolean result = encryption.init();
        if (!result) {
          infoStorage.put(KEY_NO_CRYPTO, true);
          encryptionMethod = EncryptionMethod.NO_ENCRYPTION;
        }
        break;
      case MEDIUM:
        encryption = new AesEncryption(cryptoStorage, null);
        if (encryption.init()) {
          infoStorage.put(KEY_NO_CRYPTO, true);
          encryptionMethod = EncryptionMethod.NO_ENCRYPTION;
        }
        break;
    }
  }

  /**
   * Callback interface to make actions on another place and execute code
   * based on a result of action
   * onSuccess function will be called when action is successful
   * onFail function will be called when action fails due to a reason
   */
  public interface Callback {
    void onSuccess();

    void onFail(Exception e);
  }
}
