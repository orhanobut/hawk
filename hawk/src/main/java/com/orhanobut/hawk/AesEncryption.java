package com.orhanobut.hawk;

import android.text.TextUtils;

import java.security.GeneralSecurityException;


/**
 * Provides AES algorithm
 *
 * @author Orhan Obut
 */
final class AesEncryption implements Encryption {

    private static final String KEY_SALT = "asdf3242klj";

    private AesCbcWithIntegrity.SecretKeys key;
    private String saltKey;
    private Storage storage;

    AesEncryption(Storage storage, String password) {
        this.storage = storage;
        this.saltKey = storage.get(KEY_SALT);
        generateSecretKey(password);
    }

    @Override
    public String encrypt(byte[] value) {
        if (value == null) {
            return null;
        }
        String result = null;
        try {
            AesCbcWithIntegrity.CipherTextIvMac civ = AesCbcWithIntegrity.encrypt(value, key);
            result = civ.toString();
        } catch (GeneralSecurityException e) {
            Logger.d(e.getMessage());
        }

        return result;
    }

    @Override
    public byte[] decrypt(String value) {
        if (value == null) {
            return null;
        }
        byte[] result = null;

        try {
            AesCbcWithIntegrity.CipherTextIvMac civ = getCipherTextIvMac(value);
            result = AesCbcWithIntegrity.decrypt(civ, key);
        } catch (GeneralSecurityException e) {
            Logger.d(e.getMessage());
        }

        return result;
    }

    @Override
    public boolean reset() {
        return storage.clear();
    }

    private AesCbcWithIntegrity.CipherTextIvMac getCipherTextIvMac(String cipherText) {
        return new AesCbcWithIntegrity.CipherTextIvMac(cipherText);
    }

    /**
     * Gets the secret key by using salt and password. Salt is stored in the storage
     * If the salt is not stored, that means it is first time and it creates the salt and
     * save it in the storage
     */
    private void generateSecretKey(String password) {
        try {
            if (TextUtils.isEmpty(saltKey)) {
                saltKey = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt());
                storage.put(KEY_SALT, saltKey);
            }

            key = AesCbcWithIntegrity.generateKeyFromPassword(password, saltKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

}