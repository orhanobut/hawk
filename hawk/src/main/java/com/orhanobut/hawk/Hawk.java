package com.orhanobut.hawk;

import android.content.Context;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author Orhan Obut
 */
public final class Hawk {

    private static final String TAG = "HAWK";
    private static final String TAG_CRYPTO = "324909sdfsd98098";

    private static Encoder encoder;
    private static Storage storage;
    private static Encryption encryption;
    private static LogLevel logLevel;

    private Hawk() {
        // no instance
    }

    public static void init(Context context) {
        init(context, LogLevel.FULL);
    }

    /**
     * This method must be called in order to initiate the hawk
     *
     * @param context is used to instantiate context based objects. ApplicationContext will be used
     */
    public static void init(Context context, LogLevel logLevel) {
        Context appContext = context.getApplicationContext();
        Hawk.logLevel = logLevel;
        Hawk.storage = new SharedPreferencesStorage(appContext, TAG);
        Hawk.encryption = new AesEncryption(new SharedPreferencesStorage(appContext, TAG_CRYPTO));
        Hawk.encoder = new HawkEncoder(encryption, new GsonParser(new Gson()));
    }

    /**
     * Saves every type of Objects. List, List<T>, primitives
     *
     * @param key   is used to save the data
     * @param value is the data that is gonna be saved. Value can be object, list type, primitives
     */
    public static <T> void put(String key, T value) {
        String cipherText = encoder.encode(value);
        String fullText = DataUtil.addType(cipherText, value.getClass(), false);
        storage.put(key, fullText);
    }

    /**
     * @param key is used to get the saved data
     * @return the saved object
     */
    public static <T> T get(String key) {
        String fullText = storage.get(key);
        try {
            return encoder.decode(fullText);
        } catch (Exception e) {
            e.printStackTrace();
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
        String fullText = storage.get(key);
        T t;
        try {
            t = encoder.decode(fullText);
        } catch (Exception e) {
            return null;
        }
        if (t == null) {
            return defaultValue;
        }
        return null;
    }

    /**
     * Saves the list of objects to the storage
     *
     * @param key  is used to save the data
     * @param list is the data that will be saved
     */
    public static <T> void put(String key, List<T> list) {
        String cipherText = encoder.encode(list);
        Class clazz = list.get(0).getClass();
        String fullText = DataUtil.addType(cipherText, clazz, true);
        storage.put(key, fullText);
    }

    /**
     * Size of the saved data. Each key will be counted as 1
     *
     * @return the size
     */
    public static int count() {
        return storage.count();
    }

    /**
     * Clears the storage, note that crypto data won't be deleted such as salt key etc.
     * Use resetCrypto in order to clear crypto information
     */
    public static void clear() {
        storage.clear();
    }

    /**
     * Clears all saved data that is used for the crypto
     */
    public static void resetCrypto() {
        encryption.reset();
    }

    public static LogLevel getLogLevel() {
        return logLevel;
    }
}
