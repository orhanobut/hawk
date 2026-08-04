package com.orhanobut.hawk;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.os.Build;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * {@link Encryption} implementation backed by the Android Keystore.
 *
 * <p>The master key is generated inside the Android Keystore (never hard-coded, never leaves
 * secure hardware when available) and is used with AES-GCM. Every value is encrypted with a fresh
 * random 96-bit IV and authenticated with a 128-bit GCM tag. The Hawk entry key is bound to the
 * ciphertext as additional authenticated data (AAD), mirroring the entity binding Conceal used.</p>
 *
 * <p>This implementation uses only platform crypto - no native libraries.</p>
 */
public class KeystoreAesGcmEncryption implements Encryption {

  private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final int GCM_IV_LENGTH = 12;
  private static final String DEFAULT_KEY_ALIAS = "hawk-next-master-key";

  private final String keyAlias;
  private SecretKey secretKey;

  public KeystoreAesGcmEncryption() {
    this(DEFAULT_KEY_ALIAS);
  }

  /**
   * @param keyAlias the Android Keystore alias used for the AES-GCM master key
   */
  public KeystoreAesGcmEncryption(String keyAlias) {
    this.keyAlias = keyAlias;
  }

  /**
   * Test constructor: uses a fixed key instead of the Android Keystore.
   */
  KeystoreAesGcmEncryption(String keyAlias, SecretKey secretKey) {
    this.keyAlias = keyAlias;
    this.secretKey = secretKey;
  }

  @Override public boolean init() {
    try {
      getKey();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override public String encrypt(String key, String value) throws Exception {
    if (value == null) {
      return null;
    }
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, getKey());
    cipher.updateAAD(key.getBytes(StandardCharsets.UTF_8));

    byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
    byte[] iv = cipher.getIV();
    byte[] combined = new byte[iv.length + cipherText.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
    return Base64.encodeToString(combined, Base64.NO_WRAP);
  }

  @Override public String decrypt(String key, String value) throws Exception {
    if (value == null) {
      return null;
    }
    byte[] decoded = Base64.decode(value, Base64.NO_WRAP);
    if (decoded.length <= GCM_IV_LENGTH) {
      throw new IllegalArgumentException("Cipher text is too short");
    }

    ByteBuffer buffer = ByteBuffer.wrap(decoded);
    byte[] iv = new byte[GCM_IV_LENGTH];
    buffer.get(iv);
    byte[] cipherText = new byte[buffer.remaining()];
    buffer.get(cipherText);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    cipher.updateAAD(key.getBytes(StandardCharsets.UTF_8));
    return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
  }

  private SecretKey getKey() throws Exception {
    if (secretKey == null) {
      secretKey = getOrCreateKey();
    }
    return secretKey;
  }

  private SecretKey getOrCreateKey() throws Exception {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      // Android Keystore AES-GCM key generation requires API 23+.
      throw new UnsupportedOperationException(
          "Android Keystore AES-GCM encryption requires API level 23 or higher");
    }
    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
    keyStore.load(null);

    Key key = keyStore.getKey(keyAlias, null);
    if (key instanceof SecretKey) {
      return (SecretKey) key;
    }

    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
    keyGenerator.init(new KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build());
    return keyGenerator.generateKey();
  }
}
