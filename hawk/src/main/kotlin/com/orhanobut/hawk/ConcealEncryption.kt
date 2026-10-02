package com.orhanobut.hawk


import android.content.Context
import android.util.Base64
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.CryptoConfig
import com.facebook.crypto.Entity
import com.facebook.crypto.keychain.KeyChain

/** Legacy Conceal encryption, retained for Hawk 2 data and constructor compatibility. */
open class ConcealEncryption protected constructor(private val crypto: Crypto) : Encryption {
    constructor(context: Context) : this(SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256))
    protected constructor(keyChain: KeyChain) : this(AndroidConceal.get().createDefaultCrypto(keyChain))
    override fun init(): Boolean = crypto.isAvailable
    @Throws(Exception::class)
    override fun encrypt(key: String, value: String): String =
        Base64.encodeToString(crypto.encrypt(value.toByteArray(), Entity.create(key)), Base64.NO_WRAP)
    @Throws(Exception::class)
    override fun decrypt(key: String, value: String): String =
        String(crypto.decrypt(Base64.decode(value, Base64.NO_WRAP), Entity.create(key)), Charsets.UTF_8)
}
