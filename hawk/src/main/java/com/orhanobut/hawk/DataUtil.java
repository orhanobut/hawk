package com.orhanobut.hawk;

import java.io.Serializable;

/**
 * @author Orhan Obut
 */
final class DataUtil {

    private static final String DELIMITER = "@";

    private DataUtil() {
        // no instance
    }

    /**
     * Saved data contains more info than cipher text, this method will unmarshall these data
     *
     * @param storedText is the saved data
     * @return the datainfo object which contains all necessary information
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
        boolean isSerializable = text.charAt(text.length() - 1) == '1';
        boolean isList = text.charAt(text.length() - 2) == '1';
        String className = text.substring(0, text.length() - 2);
        String cipherText = storedText.substring(index);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new DataInfo(isSerializable, isList, cipherText, clazz);
    }

    /**
     * Adds some information to the cipher text
     *
     * @param cipherText is the text that will have more info
     * @param clazz      is the data type of the cipher text
     * @param isList     determines whether is the list or not in order to use this info later
     * @return the full text
     */
    static String addType(String cipherText, Class clazz, boolean isList) {
        String className = clazz.getCanonicalName();
        int serializable = Serializable.class.isAssignableFrom(clazz) ? 1 : 0;
        int list = isList ? 1 : 0;
        return className + list + serializable + DELIMITER + cipherText;
    }

}
