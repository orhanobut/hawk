package com.orhanobut.hawk;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public final class Hawk {

  private static HawkBuilder hawkBuilder;

  private Hawk() {
    // no instance
  }

  /**
   * This will init the hawk without password protection.
   *
   * @param context is used to instantiate context based objects. ApplicationContext will be used
   */
  public static HawkBuilder init(Context context) {
    if (context == null) {
      throw new NullPointerException("Context should not be null");
    }
    hawkBuilder = new HawkBuilder(context);
    return hawkBuilder;
  }

  /**
   * Saves every type of Objects. List, List<T>, primitives
   *
   * @param key   is used to save the data
   * @param value is the data that is gonna be saved. Value can be object, list type, primitives
   * @return true if put is successful
   */
  public static <T> boolean put(String key, T value) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    }
    //if the value is null, simply remove it
    if (value == null) {
      return remove(key);
    }

    String encodedText = zip(value);
    //if any exception occurs during encoding, encodedText will be null and thus operation is unsuccessful
    return encodedText != null && hawkBuilder.getStorage().put(key, encodedText);
  }

  /**
   * Creates a stream to put data, RxJava dependency is required
   *
   * @param <T> value type
   * @return Observable<Boolean>
   */
  public static <T> Observable<Boolean> putObservable(final String key, final T value) {
    Utils.checkRx();
    return Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override
      public void call(Subscriber<? super Boolean> subscriber) {
        try {
          boolean result = put(key, value);
          if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(result);
            subscriber.onCompleted();
          }
        } catch (Exception e) {
          if (!subscriber.isUnsubscribed()) {
            subscriber.onError(e);
          }
        }
      }
    });
  }

  /**
   * Encodes the given value as full text (cipher + data info)
   *
   * @param value is the given value to encode
   * @return full text as string
   */
  private static <T> String zip(T value) {
    if (value == null) {
      throw new NullPointerException("Value cannot be null");
    }
    byte[] encodedValue = hawkBuilder.getEncoder().encode(value);

    String cipherText;

    if (!hawkBuilder.isEncrypted()) {
      cipherText = DataHelper.encodeBase64(encodedValue);
    } else {
      cipherText = hawkBuilder.getEncryption().encrypt(encodedValue);
    }

    if (cipherText == null) {
      return null;
    }
    return DataHelper.addType(cipherText, value);
  }

  /**
   * @param key is used to get the saved data
   * @return the saved object
   */
  public static <T> T get(String key) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    }
    String fullText = hawkBuilder.getStorage().get(key);
    if (fullText == null) {
      return null;
    }
    DataInfo dataInfo = DataHelper.getDataInfo(fullText);
    byte[] bytes;

    if (!hawkBuilder.isEncrypted()) {
      bytes = DataHelper.decodeBase64(dataInfo.getCipherText());
    } else {
      bytes = hawkBuilder.getEncryption().decrypt(dataInfo.getCipherText());
    }

    if (bytes == null) {
      return null;
    }

    try {
      return hawkBuilder.getEncoder().decode(bytes, dataInfo);
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
   * Creates a stream of data
   * RxJava dependency is required
   *
   * @param key of the data
   * @param <T> type of the data
   * @return Observable<T>
   */
  public static <T> Observable<T> getObservable(String key) {
    Utils.checkRx();
    return getObservable(key, null);
  }

  /**
   * Creates a stream of data
   * RxJava dependency is required
   *
   * @param key          of the data
   * @param defaultValue of the default value if the value doesn't exists
   * @param <T>          type of the data
   * @return Observable</T>
   */
  public static <T> Observable<T> getObservable(final String key, final T defaultValue) {
    Utils.checkRx();
    return Observable.create(new Observable.OnSubscribe<T>() {
      @Override
      public void call(Subscriber<? super T> subscriber) {
        try {
          T t = get(key, defaultValue);
          if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(t);
            subscriber.onCompleted();
          }
        } catch (Exception e) {
          if (!subscriber.isUnsubscribed()) {
            subscriber.onError(e);
          }
        }
      }
    });
  }

  /**
   * Enables chaining of multiple put invocations.
   *
   * @return a simple chaining object
   */
  public static Chain chain() {
    return new Chain();
  }

  /**
   * Enables chaining of multiple put invocations.
   *
   * @param capacity the amount of put invocations you're about to do
   * @return a simple chaining object
   */
  public static Chain chain(int capacity) {
    return new Chain(capacity);
  }

  /**
   * Size of the saved data. Each key will be counted as 1
   *
   * @return the size
   */
  public static long count() {
    return hawkBuilder.getStorage().count();
  }

  /**
   * Clears the storage, note that crypto data won't be deleted such as salt key etc.
   * Use resetCrypto in order to clear crypto information
   *
   * @return true if clear is successful
   */
  public static boolean clear() {
    return hawkBuilder.getStorage().clear();
  }

  /**
   * Removes the given key/value from the storage
   *
   * @param key is used for removing related data from storage
   * @return true if remove is successful
   */
  public static boolean remove(String key) {
    return hawkBuilder.getStorage().remove(key);
  }

  /**
   * Removes values associated with the given keys from the storage
   *
   * @param keys are used for removing related data from storage
   * @return true if all removals are successful
   */
  public static boolean remove(String... keys) {
    return hawkBuilder.getStorage().remove(keys);
  }

  /**
   * Checks the given key whether it exists or not
   *
   * @param key is the key to check
   * @return true if it exists in the storage
   */
  public static boolean contains(String key) {
    return hawkBuilder.getStorage().contains(key);
  }

  /**
   * Clears all saved data that is used for the crypto
   *
   * @return true if reset is successful
   */
  public static boolean resetCrypto() {
    return hawkBuilder.getEncryption() == null || hawkBuilder.getEncryption().reset();
  }

  public static LogLevel getLogLevel() {
    if (hawkBuilder == null) {
      return LogLevel.NONE;
    }
    return hawkBuilder.getLogLevel();
  }

  /**
   * Provides the ability to chain put invocations:
   * <code>Hawk.chain().put("foo", 0).put("bar", false).commit()</code>
   * <p/>
   * <code>commit()</code> writes the chain values to persistent storage. Omitting it will
   * result in all chained data being lost.
   */
  public static final class Chain {

    private final List<Pair<String, ?>> items;

    public Chain() {
      this(10);
    }

    public Chain(int capacity) {
      items = new ArrayList<>(capacity);
    }

    /**
     * Saves every type of Objects. List, List<T>, primitives
     *
     * @param key   is used to save the data
     * @param value is the data that is gonna be saved. Value can be object, list type, primitives
     */
    public <T> Chain put(String key, T value) {
      if (key == null) {
        throw new NullPointerException("Key cannot be null");
      }
      String encodedText = zip(value);
      if (encodedText == null) {
        Log.d("HAWK", "Key : " + key + " is not added, encryption failed");
        return this;
      }
      items.add(new Pair<>(key, encodedText));
      return this;
    }

    /**
     * Commits the chained values to storage.
     *
     * @return true if successfully saved, false otherwise.
     */
    public boolean commit() {
      return hawkBuilder.getStorage().put(items);
    }

  }


}
