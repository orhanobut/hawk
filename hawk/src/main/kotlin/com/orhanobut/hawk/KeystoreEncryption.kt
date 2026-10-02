package com.orhanobut.hawk

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * AES-256-GCM encryption with a key owned by this application's Android Keystore.
 * Existing ciphertext requires the same alias and key; values cannot be restored to
 * a different installation by backing up preferences alone. No plaintext fallback is used.
 */
open class KeystoreEncryption internal constructor(private val keys: EncryptionKeys) : Encryption {
    /** Use a stable alias for as long as its encrypted values are retained. */
    @JvmOverloads
    constructor(keyAlias: String = "com.orhanobut.hawk.aes-gcm") : this(AndroidKeystoreKeys(keyAlias))

    override fun init(): Boolean = try {
        keys.getOrCreate()
        true
    } catch (_: Exception) {
        false
    }

    @Throws(Exception::class)
    override fun encrypt(key: String, value: String): String = PREFIX + Base64.encodeToString(
        AesGcmCipher.encrypt(key, value, keys.getOrCreate()), Base64.NO_WRAP
    )

    @Throws(Exception::class)
    override fun decrypt(key: String, value: String): String {
        require(value.startsWith(PREFIX)) { "Unsupported encryption format; migrate legacy values before upgrading" }
        val envelope = Base64.decode(value.substring(PREFIX.length), Base64.NO_WRAP)
        // Never create or replace a missing key while reading existing data.
        return AesGcmCipher.decrypt(key, envelope, keys.get())
    }

    private companion object { const val PREFIX = "hawk-aes-gcm:" }
}

internal interface EncryptionKeys {
    fun getOrCreate(): SecretKey
    fun get(): SecretKey
}

private class AndroidKeystoreKeys(private val alias: String) : EncryptionKeys {
    init { require(alias.isNotBlank()) { "Keystore alias must not be blank" } }

    override fun getOrCreate(): SecretKey = synchronized(lock) {
        val store = openStore()
        if (store.containsAlias(alias)) return@synchronized readKey(store)
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build())
            generateKey()
        }
    }

    override fun get(): SecretKey = readKey(openStore())

    private fun openStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun readKey(store: KeyStore): SecretKey {
        val key = store.getKey(alias, null) as? SecretKey
            ?: throw KeyStoreException("Hawk encryption key is missing or has an incompatible type")
        if (key.algorithm != "AES") throw KeyStoreException("Hawk encryption key must use AES")
        return key
    }

    private companion object { val lock = Any() }
}
