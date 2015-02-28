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

    /**
     * This method must be called in order to initiate the hawk
     *
     * @param context  is used to instantiate context based objects. ApplicationContext will be used
     * @param password is used for key generation
     */
    public static void init(Context context, String password) {
        init(context, password, LogLevel.NONE);
    }

    /**
     * This method must be called in order to initiate the hawk
     *
     * @param context  is used to instantiate context based objects. ApplicationContext will be used
     * @param password is used for key generation
     * @param logLevel is used for logging
     */
    public static void init(Context context, String password, LogLevel logLevel) {
        Context appContext = context.getApplicationContext();
        Hawk.logLevel = logLevel;
        Hawk.storage = new SharedPreferencesStorage(appContext, TAG);
        Hawk.encryption = new AesEncryption(new SharedPreferencesStorage(appContext, TAG_CRYPTO), password);
        Hawk.encoder = new HawkEncoder(encryption, new GsonParser(new Gson()));
    }

    /**
     * Saves every type of Objects. List, List<T>, primitives
     *
     * @param key   is used to save the data
     * @param value is the data that is gonna be saved. Value can be object, list type, primitives
     * @return true if put is successful
     */
    public static <T> boolean put(String key, T value) {
        return put(key, value, null, false, true) != null;
    }

    /**
     * @param key is used to get the saved data
     * @return the saved object
     */
    public static <T> T get(String key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        String fullText = storage.get(key);
        try {
            return encoder.decode(fullText);
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
     * Saves the list of objects to the storage
     *
     * @param key  is used to save the data
     * @param list is the data that will be saved
     * @return true if put is successful
     */
    public static <T> boolean put(String key, List<T> list) {
        return put(key, null, list, true, true) != null;
    }

    private static <T> String put(String key, T value, List<T> list, boolean mustHaveList,
                                  boolean addToStorage) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }

        if (mustHaveList) {
            if (list == null) {
                throw new NullPointerException("List<T> may not be null");
            } else if (list.isEmpty()) {
                throw new IllegalArgumentException("List<T> may not be empty");
            }
        } else {
            if (value == null) {
                throw new NullPointerException("Value cannot be null");
            }
        }

        String cipherText;
        if (mustHaveList) {
            cipherText = encoder.encode(list);
        } else {
            cipherText = encoder.encode(value);
        }

        if (cipherText == null) {
            return null;
        }

        String fullText;
        if (mustHaveList) {
            fullText = DataUtil.addType(cipherText, list.get(0).getClass(), true);
        } else {
            fullText = DataUtil.addType(cipherText, value.getClass(), false);
        }

        boolean successful = true;
        if (addToStorage) {
            successful = storage.put(key, fullText);
        }

        return successful ? fullText : null;
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
     *
     * @return true if clear is successful
     */
    public static boolean clear() {
        return storage.clear();
    }

    /**
     * Removes the given key/value from the storage
     *
     * @param key is used for removing related data from storage
     * @return true if remove is successful
     */
    public static boolean remove(String key) {
        return storage.remove(key);
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
     *
     * @return true if reset is successful
     */
    public static boolean resetCrypto() {
        return encryption.reset();
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
            String fullText = Hawk.put(key, value, null, false, false);
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
            String fullText = Hawk.put(key, null, list, true, false);
            items.add(new Pair<String, Object>(key, fullText));
            return this;
        }

        /**
         * Saves the chained values.
         */
        public boolean done() {
            return storage.put(items);
        }

    }

}
