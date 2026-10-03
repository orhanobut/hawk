package com.orhanobut.hawk


import android.content.Context
import com.google.gson.Gson

/** Configures the storage pipeline. Null setters restore the corresponding default. */
open class HawkBuilder(context: Context?) {
    private val context: Context
    private var storage: Storage? = null
    private var parser: Parser? = null
    private var serializer: Serializer? = null
    private var converter: Converter? = null
    private var encryption: Encryption? = null
    private var logInterceptor: LogInterceptor? = null
    init {
        HawkUtils.checkNull("Context", context)
        this.context = context!!.applicationContext
    }
    open fun setStorage(storage: Storage?): HawkBuilder = apply { this.storage = storage }
    open fun setParser(parser: Parser?): HawkBuilder = apply { this.parser = parser }
    open fun setSerializer(serializer: Serializer?): HawkBuilder = apply { this.serializer = serializer }
    open fun setConverter(converter: Converter?): HawkBuilder = apply { this.converter = converter }
    open fun setEncryption(encryption: Encryption?): HawkBuilder = apply { this.encryption = encryption }
    open fun setLogInterceptor(logInterceptor: LogInterceptor?): HawkBuilder = apply { this.logInterceptor = logInterceptor }
    @JvmName("getLogInterceptor") internal fun getLogInterceptor(): LogInterceptor =
        logInterceptor ?: LogInterceptor {}.also { logInterceptor = it }
    @JvmName("getStorage") internal fun getStorage(): Storage =
        storage ?: SharedPreferencesStorage(context, "Hawk2").also { storage = it }
    @JvmName("getParser") internal fun getParser(): Parser = parser ?: GsonParser(Gson()).also { parser = it }
    @JvmName("getConverter") internal fun getConverter(): Converter = converter ?: HawkConverter(getParser()).also { converter = it }
    @JvmName("getSerializer") internal fun getSerializer(): Serializer = serializer ?: HawkSerializer(getLogInterceptor()).also { serializer = it }
    @JvmName("getEncryption") internal fun getEncryption(): Encryption = encryption ?: run {
        KeystoreEncryption().also {
            check(it.init()) { "Unable to initialize Android Keystore encryption" }
            encryption = it
        }
    }
    open fun build() { Hawk.build(this) }
}
