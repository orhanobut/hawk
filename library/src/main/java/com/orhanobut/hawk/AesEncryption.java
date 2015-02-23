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
    private static final String PASSWORD = "asdf39230234242klj";

    private String saltKey;
    private Storage storage;

    AesEncryption(Storage storage) {
        this.storage = storage;
        this.saltKey = storage.get(KEY_SALT);
    }

    @Override
    public String encrypt(byte[] value) {
        if (value == null) {
            return null;
        }
        String result = null;
        try {
            AesCbcWithIntegrity.SecretKeys key = getSecretKey();
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
            AesCbcWithIntegrity.SecretKeys key = getSecretKey();
            AesCbcWithIntegrity.CipherTextIvMac civ = getCipherTextIvMac(value);
            result = AesCbcWithIntegrity.decrypt(civ, key);
        } catch (GeneralSecurityException e) {
            Logger.d(e.getMessage());
        }

        return result;
    }

    @Override
    public void reset() {
        storage.clear();
    }

    private AesCbcWithIntegrity.CipherTextIvMac getCipherTextIvMac(String cipherText) {
        return new AesCbcWithIntegrity.CipherTextIvMac(cipherText);
    }

    /**
     * Gets the secret key by using salt and password. Salt is stored in the storage
     * If the salt is not stored, that means it is first time and it creates the salt and
     * save it in the storage
     *
     * @return the secret key
     */
    private AesCbcWithIntegrity.SecretKeys getSecretKey() {
        AesCbcWithIntegrity.SecretKeys key = null;
        try {
            if (!TextUtils.isEmpty(saltKey)) {
                key = AesCbcWithIntegrity.generateKeyFromPassword(PASSWORD, saltKey);
            }

            // already generated
            if (key != null) {
                return key;
            }
            saltKey = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt());
            key = AesCbcWithIntegrity.generateKeyFromPassword(PASSWORD, saltKey);
            storage.put(KEY_SALT, saltKey);
        } catch (GeneralSecurityException e) {
            Logger.d(e.getMessage());
        }

        return key;
    }

}