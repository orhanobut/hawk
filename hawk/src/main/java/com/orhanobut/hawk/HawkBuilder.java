package com.orhanobut.hawk;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

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
  private Encoder encoder;
  private Parser parser;
  private Encryption encryption;
  private Callback callback;

  public enum EncryptionMethod {
    HIGHEST, MEDIUM, NO_ENCRYPTION
  }

  public HawkBuilder(Context context) {
    if (context == null) {
      throw new NullPointerException("Context should not be null");
    }
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

  public HawkBuilder setCallback(Callback callback) {
    this.callback = callback;
    return this;
  }

  public HawkBuilder setParser(Parser parser) {
    this.parser = parser;
    return this;
  }

  public Context getContext() {
    return context;
  }

  public EncryptionMethod getEncryptionMethod() {
    if (encryptionMethod == null) {
      encryptionMethod = EncryptionMethod.MEDIUM;
    }
    return encryptionMethod;
  }

  public String getPassword() {
    return password;
  }

  public LogLevel getLogLevel() {
    if (logLevel == null) {
      logLevel = LogLevel.NONE;
    }
    return logLevel;
  }

  public Storage getStorage() {
    if (cryptoStorage == null) {
      cryptoStorage = new SharedPreferencesStorage(context, TAG);
    }
    return cryptoStorage;
  }

  public Encoder getEncoder() {
    if (encoder == null) {
      encoder = new HawkEncoder(getParser());
    }
    return encoder;
  }

  public Storage getInfoStorage() {
    return new SharedPreferencesStorage(context, TAG_INFO);
  }

  public Parser getParser() {
    if (parser == null) {
      parser = new GsonParser(new Gson());
    }
    return parser;
  }

  public Encryption getEncryption() {
    return encryption;
  }

  public boolean isEncrypted() {
    return encryptionMethod != EncryptionMethod.NO_ENCRYPTION;
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
    if (callback != null) {
      new Handler().post(new Runnable() {
        @Override public void run() {
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
    setEncryption();
  }

  private void setEncryption() {
    switch (getEncryptionMethod()) {
      case NO_ENCRYPTION:
        break;
      case HIGHEST:
        encryption = new AesEncryption(getStorage(), getPassword());
        if (!getEncryption().init()) {
          getInfoStorage().put(KEY_NO_CRYPTO, true);
          encryptionMethod = EncryptionMethod.NO_ENCRYPTION;
        }
        break;
      case MEDIUM:
        encryption = new AesEncryption(getStorage(), null);
        if (!getEncryption().init()) {
          getInfoStorage().put(KEY_NO_CRYPTO, true);
          encryptionMethod = EncryptionMethod.NO_ENCRYPTION;
        }
        break;
      default:
        throw new IllegalStateException("Encryption mode is not correct");
    }
  }

  public static Storage newSharedPrefStorage(Context context) {
    return new SharedPreferencesStorage(context, TAG);
  }

  public static Storage newSqliteStorage(Context context) {
    return new SqliteStorage(context);
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

  public Observable<Boolean> buildRx() {
    Utils.checkRx();
    return Observable.defer(new Func0<Observable<Boolean>>() {
      @Override public Observable<Boolean> call() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
          @Override public void call(Subscriber<? super Boolean> subscriber) {
            try {
              startBuild();
              subscriber.onNext(true);
              subscriber.onCompleted();
            } catch (Exception e) {
              subscriber.onError(e);
            }
          }
        });
      }
    });
  }
}
