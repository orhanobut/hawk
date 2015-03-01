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
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        String cipherText = encoder.encode(value);
        //if any exception occurs during encoding, cipherText will be null and thus operation is unsuccessful
        if (cipherText == null) {
            return false;
        }
        String fullText = DataUtil.addType(cipherText, value.getClass(), false);
        return storage.put(key, fullText);
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
        if (list == null) {
            throw new NullPointerException("List<T> may not be null");
        }
        if (list.size() == 0) {
            throw new NullPointerException("List<T> cannot be empty");
        }
        String cipherText = encoder.encode(list);
        //if any exception occurs during encoding, cipherText will be null and thus operation is unsuccessful
        if (cipherText == null) {
            return false;
        }
        Class clazz = list.get(0).getClass();
        String fullText = DataUtil.addType(cipherText, clazz, true);
        return storage.put(key, fullText);
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
            if (value == null) {
                throw new NullPointerException("Value cannot be null");
            }
            String cipherText = encoder.encode(value);
            //if any exception occurs during encoding, cipherText will be null and thus operation is unsuccessful
            if (cipherText == null) {
                throw new IllegalStateException("chain failed for key: " + key);
            }
            String fullText = DataUtil.addType(cipherText, value.getClass(), false);
            items.add(new Pair<>(key, fullText));
            return this;
        }

        /**
         * Saves the list of objects to the storage
         *
         * @param key  is used to save the data
         * @param list is the data that will be saved
         */
        public <T> Chain put(String key, List<T> list) {
            if (list == null) {
                throw new NullPointerException("List<T> may not be null");
            }
            if (list.size() == 0) {
                throw new NullPointerException("List<T> cannot be empty");
            }
            String cipherText = encoder.encode(list);
            //if any exception occurs during encoding, cipherText will be null and thus operation is unsuccessful
            if (cipherText == null) {
                throw new IllegalStateException("chain failed for key: " + key);
            }
            Class clazz = list.get(0).getClass();
            String fullText = DataUtil.addType(cipherText, clazz, true);
            items.add(new Pair<>(key, fullText));
            return this;
        }

        /**
         * Commits the chained values to storage.
         *
         * @return true if successfully saved, false otherwise.
         */
        public boolean commit() {
            return storage.put(items);
        }

    }

}
