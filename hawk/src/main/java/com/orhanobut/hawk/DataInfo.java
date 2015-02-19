package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
final class DataInfo {

    private final boolean isSerializable;
    private final boolean isList;
    private final String cipherText;
    private final Class clazz;

    DataInfo(boolean isSerializable, boolean isList, String cipherText, Class clazz) {
        this.isSerializable = isSerializable;
        this.isList = isList;
        this.cipherText = cipherText;
        this.clazz = clazz;
    }

    boolean isSerializable() {
        return isSerializable;
    }

    boolean isList() {
        return isList;
    }

    String getCipherText() {
        return cipherText;
    }

    Class getClazz() {
        return clazz;
    }
}
