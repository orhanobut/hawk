package com.orhanobut.hawk;

import java.io.Serializable;

/**
 * @author Orhan Obut
 */
final class DataUtil {

    private static final String DELIMITER = "@";
    private static final char FLAG_NON_SERIALIZABLE = '0';
    private static final char FLAG_SERIALIZABLE = '1';
    private static final char TYPE_LIST = '1';
    private static final char TYPE_OBJECT = '0';

    private DataUtil() {
        // no instance
    }

    /**
     * Saved data contains more info than cipher text, this method will demarshall the data
     *
     * @param storedText is the saved data
     * @return the DataInfo object which contains all necessary information
     */
    static DataInfo getDataInfo(String storedText) {
        if (storedText == null) {
            throw new NullPointerException("Text should not be null");
        }
        int index = storedText.indexOf(DELIMITER);
        if (index == 0) {
            throw new IllegalArgumentException("Text should contain delimiter");
        }
        String text = storedText.substring(0, index);
        boolean isSerializable = text.charAt(text.length() - 1) == FLAG_SERIALIZABLE;
        boolean isList = text.charAt(text.length() - 2) == TYPE_LIST;
        String className = text.substring(0, text.length() - 2);
        String cipherText = storedText.substring(index);

        // if it is not list and serializable, no need to create the class object
        Class<?> clazz = null;
        if (isList || !isSerializable) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                Logger.d(e.getMessage());
            }
        }

        return new DataInfo(isSerializable, isList, cipherText, clazz);
    }

    /**
     * Adds some information to the cipher text
     *
     * @param cipherText is the text that will have more info
     * @param clazz      is the data type of the object
     * @return the full text
     */
    static String addTypeAsObject(String cipherText, Class clazz) {
        String className = clazz.getCanonicalName();
        char serializable = Serializable.class.isAssignableFrom(clazz) ? FLAG_SERIALIZABLE : FLAG_NON_SERIALIZABLE;
        return className + TYPE_OBJECT + serializable + DELIMITER + cipherText;
    }

    /**
     * Adds some information to the cipher text
     *
     * @param cipherText is the text that will have more info
     * @param clazz      is the data type of the list item
     * @return the full text
     */
    static String addTypeAsList(String cipherText, Class clazz) {
        String className = clazz.getCanonicalName();
        return className + TYPE_LIST + FLAG_NON_SERIALIZABLE + DELIMITER + cipherText;
    }


}
