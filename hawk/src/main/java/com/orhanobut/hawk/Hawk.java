package com.orhanobut.hawk;

import android.content.Context;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
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
        init(context, LogLevel.NONE);
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
        throwExceptionIf(key == null, "Key cannot be null");
        throwExceptionIf(value == null, "Value cannot be null");

        String fullText = getMarshalledValue(value, null);
        storage.put(key, fullText);
    }

    /**
     * @param key is used to get the saved data
     * @return the saved object
     */
    public static <T> T get(String key) {
        throwExceptionIf(key == null, "key cannot be null");

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
        T t = get(key);
        if (t == null) {
            return defaultValue;
        }
        return t;
    }

    /**
     * Saves the list of objects to the storage
     *
     * @param key  is used to save the data
     * @param list is the data that will be saved
     */
    public static <T> void put(String key, List<T> list) {
        throwExceptionIf(key == null, "Key cannot be null");
        throwExceptionIf(list == null, "List<T> may not be null");
        throwExceptionIf(list.size() == 0, "List<T> may not be null");

        String fullText = getMarshalledValue(list.get(0), list);
        storage.put(key, fullText);
    }

    /**
     * Enables chaining of multiple put invocations.
     *
     * @return a simple chaining object
     */
    public static HawkChain chain() {
        return new HawkChain();
    }

    /**
     * Enables chaining of multiple put invocations.
     *
     * @param capacity the amount of put invocations you're about to do
     * @return a simple chaining object
     */
    public static HawkChain chain(int capacity) {
        return new HawkChain(capacity);
    }

    private static <T> String getMarshalledValue(T value, List<T> list) {
        String cipherText;

        boolean isList = (null != list);
        if (isList) {
            cipherText = encoder.encode(list);
        } else {
            cipherText = encoder.encode(value);
        }

        return DataUtil.addType(cipherText, value.getClass(), isList);
    }

    private static void throwExceptionIf(boolean failure, String description) {
        if (failure) {
            throw new NullPointerException(description);
        }
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
     * Removes the given key/value from the storage
     */
    public static void remove(String key) {
        storage.remove(key);
    }

    /**
     * Checks the given key whether it exists or not
     *
     * @param key is the key to check
     * @return true if it exists in the storage
     */
    public static boolean contains(String key) {
        return storage.contains(key);
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

    /**
     * Provides the ability to chain put invocations, for example
     * <code>Hawk.chain().put("foo", 0).put("bar", 1).done()</code>
     */
    public static final class HawkChain {

        private final List<Pair<String, ?>> items;

        public HawkChain() {
            this(4);
        }

        public HawkChain(int capacity) {
            items = new ArrayList<>(capacity);
        }

        /**
         * Saves every type of Objects. List, List<T>, primitives
         *
         * @param key   is used to save the data
         * @param value is the data that is gonna be saved. Value can be object, list type, primitives
         */
        public <T> HawkChain put(String key, T value) {
            throwExceptionIf(key == null, "Key cannot be null");
            throwExceptionIf(value == null, "Value cannot be null");

            String fullText = getMarshalledValue(value, null);
            items.add(new Pair<String, Object>(key, fullText));

            return this;
        }

        /**
         * Saves the list of objects to the storage
         *
         * @param key  is used to save the data
         * @param list is the data that will be saved
         */
        public <T> HawkChain put(String key, List<T> list) {
            throwExceptionIf(key == null, "Key cannot be null");
            throwExceptionIf(list == null, "List<T> may not be null");
            throwExceptionIf(list.size() == 0, "List<T> may not be null");

            String fullText = getMarshalledValue(list.get(0), list);
            items.add(new Pair<String, Object>(key, fullText));

            return this;
        }

        /**
         * Saves the chained values.
         */
        public void done() {
            storage.put(items);
        }

    }

}
