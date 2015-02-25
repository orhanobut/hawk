package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
final class DataInfo {

    public final boolean isSerializable;
    public final boolean isList;
    public final String cipherText;
    public final Class clazz;

    DataInfo(boolean isSerializable, boolean isList, String cipherText, Class clazz) {
        this.isSerializable = isSerializable;
        this.isList = isList;
        this.cipherText = cipherText;
        this.clazz = clazz;
    }

}
