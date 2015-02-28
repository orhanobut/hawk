package com.orhanobut.hawk;

import android.content.Context;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        ensureKeyIsValid(key);
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

    /*
     * A helper put method.
     *
     * @param key          the key
     * @param value        the value if isList is false, otherwise null
     * @param list         the list, if isList is true, otherwise null
     * @param isList       determines if a list instance should be present
     * @param addToStorage determines if the keyval should be immediately added to storage
     */
    private static <T> String put(String key, T value, List<T> list, boolean isList,
                                  boolean addToStorage) {
        ensureKeyIsValid(key);
        if (isList) {
            ensureListIsValid(list);
        } else {
            ensureValueIsValid(value);
        }

        String cipherText;
        if (isList) {
            cipherText = encoder.encode(list);
        } else {
            cipherText = encoder.encode(value);
        }

        if (cipherText == null) {
            return null;
        }

        String fullText;
        if (isList) {
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

    private static <T> void ensureListIsValid(List<T> list) {
        if (list == null) {
            throw new NullPointerException("List<T> may not be null");
        } else if (list.isEmpty()) {
            throw new IllegalArgumentException("List<T> may not be empty");
        }
    }

    private static <T> void ensureValueIsValid(T value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
    }

    private static void ensureKeyIsValid(String key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
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
        private boolean atomic;

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
            addItem(key, fullText);
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
            addItem(key, fullText);
            return this;
        }

        /**
         * If true, Hawk throws an exception if any of the chained invocations fails.
         * Disabled by default.
         *
         * @param atomic true to throw an exception, false otherwise.
         */
        public HawkChain atomic(boolean atomic) {
            this.atomic = atomic;
            return this;
        }

        /**
         * Saves the chained values.
         */
        public boolean done() {
            return storage.put(items);
        }

        /**
         * Saves the chained values and returns the saved keys.
         *
         * @return the set of saved keys.
         */
        public Set<String> doneWithKeys() {
            boolean done = storage.put(items);

            Set<String> keys;
            if (done) {
                keys = new HashSet<>(items.size());
                for (Pair<String, ?> p : items) {
                    keys.add(p.first);
                }
            } else {
                keys = Collections.emptySet();
            }

            return keys;
        }

        private void addItem(String key, String data) {
            if (data == null) {
                if (atomic) {
                    throw new IllegalStateException("chain failed for key: " + key);
                }
            } else {
                items.add(new Pair<>(key, data));
            }
        }

    }

}
