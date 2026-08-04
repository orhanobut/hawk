package com.orhanobut.hawk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Decrypts values that were encrypted by Hawk 2.x with Facebook Conceal 1.1.3.
 *
 * <p>The legacy format was verified against the Conceal 1.1.3 source code
 * (facebookarchive/conceal, tag v.1.1.3):</p>
 *
 * <pre>
 * payload  = [1 byte serialization version][1 byte cipher id][12 byte IV][AES-GCM ciphertext][16 byte tag]
 * AAD      = [serialization version][cipher id][entity (Hawk key, UTF-8)]
 * key      = 256-bit AES key generated with SecureRandom, Base64 encoded by Conceal's
 *            SharedPrefsBackedKeyChain and stored in SharedPreferences "crypto.KEY_256"
 *            under the preference "cipher_key" (16-bit variant: "crypto"/KEY_128).
 * encoding = the whole payload is Base64 encoded (NO_WRAP) by Hawk's ConcealEncryption.
 * </pre>
 *
 * <p>This class contains no Conceal code and loads no native library. It re-implements only the
 * documented standard AES-GCM layout so that existing users can migrate their data.</p>
 */
final class LegacyConcealDecryptor {

  static final byte CIPHER_SERIALIZATION_VERSION = 1;
  static final byte CIPHER_ID_128 = 1;
  static final byte CIPHER_ID_256 = 2;

  static final String SHARED_PREF_NAME_128 = "crypto";
  static final String SHARED_PREF_NAME_256 = "crypto.KEY_256";
  static final String CIPHER_KEY_PREF = "cipher_key";

  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final int IV_LENGTH = 12;

  private final SharedPreferences prefs128;
  private final SharedPreferences prefs256;

  LegacyConcealDecryptor(Context context) {
    prefs128 = context.getSharedPreferences(SHARED_PREF_NAME_128, Context.MODE_PRIVATE);
    prefs256 = context.getSharedPreferences(SHARED_PREF_NAME_256, Context.MODE_PRIVATE);
  }

  /**
   * @return the decrypted plain text, or null when the value is not in the Conceal 1.1.3 format
   *         or cannot be decrypted (missing key, wrong entity, corrupted data)
   */
  String tryDecrypt(String key, String base64CipherText) {
    if (key == null || base64CipherText == null) {
      return null;
    }
    byte[] payload;
    try {
      payload = Base64.decode(base64CipherText, Base64.NO_WRAP);
    } catch (Exception e) {
      return null;
    }
    if (payload.length < 2) {
      return null;
    }

    byte version = payload[0];
    byte cipherId = payload[1];
    if (version != CIPHER_SERIALIZATION_VERSION
        || (cipherId != CIPHER_ID_128 && cipherId != CIPHER_ID_256)) {
      return null;
    }

    SecretKey secretKey = loadCipherKey(cipherId);
    if (secretKey == null) {
      return null;
    }

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      byte[] iv = Arrays.copyOfRange(payload, 2, 2 + IV_LENGTH);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      cipher.updateAAD(new byte[] {version, cipherId});
      cipher.updateAAD(key.getBytes(StandardCharsets.UTF_8));
      byte[] plain = cipher.doFinal(payload, 2 + IV_LENGTH, payload.length - 2 - IV_LENGTH);
      return new String(plain, StandardCharsets.UTF_8);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * @return true when the payload begins with the Conceal serialization version byte, i.e. it was
   *         almost certainly produced by Conceal and must not be treated as plaintext
   */
  boolean looksLikeConceal(String base64CipherText) {
    if (base64CipherText == null) {
      return false;
    }
    try {
      byte[] payload = Base64.decode(base64CipherText, Base64.NO_WRAP);
      return payload.length > 0 && payload[0] == CIPHER_SERIALIZATION_VERSION;
    } catch (Exception e) {
      return false;
    }
  }

  private SecretKey loadCipherKey(byte cipherId) {
    SharedPreferences prefs = cipherId == CIPHER_ID_256 ? prefs256 : prefs128;
    String base64Key = prefs.getString(CIPHER_KEY_PREF, null);
    if (base64Key == null) {
      return null;
    }
    try {
      byte[] rawKey = Base64.decode(base64Key, Base64.DEFAULT);
      return new SecretKeySpec(rawKey, "AES");
    } catch (Exception e) {
      return null;
    }
  }
}
