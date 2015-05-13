package com.orhanobut.hawk;

import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of encoding and decoding.
 * List types will be encoded/decoded by parser
 * Serializable types will be encoded/decoded object stream
 * Not serializable objects will be encoded/decoded by parser
 *
 * @author Orhan Obut
 */
final class HawkEncoder implements Encoder {

    private final Parser parser;

    public HawkEncoder(Parser parser) {
        if (parser == null) {
            throw new NullPointerException("Parser may not be null");
        }
        this.parser = parser;
    }

    @Override
    public <T> byte[] encode(T value) {
        if (value == null) {
            return null;
        }

        byte[] bytes;
        if (value instanceof Serializable) {
            bytes = fromSerializable((Serializable) value);
        } else {
            String json = parser.toJson(value);
            bytes = json.getBytes();
        }

        return bytes;
    }

    @Override
    public <T> byte[] encode(List<T> value) {
        if (value == null) {
            return null;
        }
        String json = parser.toJson(value);
        return json.getBytes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T decode(byte[] bytes, DataInfo info) throws Exception {
        if (bytes == null) {
            return null;
        }
        boolean isList = info.isList();

        // if the value is not list and serializable, then use the normal deserialize
        if (!isList && info.isSerializable()) {
            return toSerializable(bytes);
        }

        // convert to the string json
        String json = new String(bytes);

        Class<?> type = info.getClazz();
        if (!isList) {
            return parser.fromJson(json, type);
        }

        return fromJsonList(json, type);
    }

    @Override
    public <T> T decodeSerializable(String value) throws Exception {
        return toSerializable(value.getBytes());
    }

    @SuppressWarnings("unchecked")
    private <T> T fromJsonList(String json, Class<?> type) throws Exception {
        ArrayList<T> list;
        list = parser.fromJson(json, new TypeToken<ArrayList<T>>() {
        }.getType());

        int size = list.size();
        for (int i = 0; i < size; i++) {
            list.set(i, (T) parser.fromJson(parser.toJson(list.get(i)), type));
        }
        return (T) list;
    }

    /**
     * used to convert object to byte buffer
     *
     * @param serializable the object to be converted
     * @return converted data
     */
    private byte[] fromSerializable(Serializable serializable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        byte[] result = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serializable);
            oos.close();
            result = baos.toByteArray();
        } catch (IOException e) {
            Logger.d(e.getMessage());
        }
        return result;
    }

    /**
     * Converts byte[] to a serializable object
     *
     * @param bytes the data
     * @param <T>   object type
     * @return the serializable object
     */
    @SuppressWarnings("unchecked")
    private <T> T toSerializable(byte[] bytes) {
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            Logger.d(e.getMessage());
        }

        if (inputStream == null) {
            return null;
        }

        T result = null;
        try {
            result = (T) inputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Logger.d(e.getMessage());
        }
        return result;
    }

}
