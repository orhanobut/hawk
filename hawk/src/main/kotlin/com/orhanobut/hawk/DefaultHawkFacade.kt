package com.orhanobut.hawk


/** Coordinates conversion, encryption, serialization and storage. */
open class DefaultHawkFacade internal constructor(
    private val storage: Storage,
    private val converter: Converter,
    private val encryption: Encryption,
    private val serializer: Serializer,
    private val logInterceptor: LogInterceptor,
) : HawkFacade {
    constructor(builder: HawkBuilder) : this(
        builder.getStorage(), builder.getConverter(), builder.getEncryption(),
        builder.getSerializer(), builder.getLogInterceptor()
    )
    init { logInterceptor.onLog("Hawk.init -> Encryption : ${encryption.javaClass.simpleName}") }
    override fun <T> put(key: String?, value: T?): Boolean {
        HawkUtils.checkNull("Key", key)
        log("Hawk.put -> key: $key, value: $value")
        if (value == null) {
            log("Hawk.put -> Value is null. Any existing value will be deleted with the given key")
            return delete(key)
        }
        val plainText = converter.toString(value)
        log("Hawk.put -> Converted to $plainText")
        if (plainText == null) { log("Hawk.put -> Converter failed"); return false }
        val cipherText = try { encryption.encrypt(key!!, plainText) } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        log("Hawk.put -> Encrypted to $cipherText")
        if (cipherText == null) { log("Hawk.put -> Encryption failed"); return false }
        val serializedText = serializer.serialize(cipherText, value)
        log("Hawk.put -> Serialized to $serializedText")
        if (serializedText == null) { log("Hawk.put -> Serialization failed"); return false }
        val success = storage.put(key, serializedText)
        log(if (success) "Hawk.put -> Stored successfully" else "Hawk.put -> Store operation failed")
        return success
    }
    override fun <T> get(key: String?): T? {
        log("Hawk.get -> key: $key")
        if (key == null) { log("Hawk.get -> null key, returning null value "); return null }
        val serializedText = storage.get<String>(key)
        log("Hawk.get -> Fetched from storage : $serializedText")
        if (serializedText == null) { log("Hawk.get -> Fetching from storage failed"); return null }
        val info = serializer.deserialize(serializedText)
        log("Hawk.get -> Deserialized")
        if (info == null) { log("Hawk.get -> Deserialization failed"); return null }
        val plainText = try { encryption.decrypt(key, info.cipherText) } catch (e: Exception) {
            log("Hawk.get -> Decrypt failed: ${e.message}")
            null
        }
        log("Hawk.get -> Decrypted to : $plainText")
        if (plainText == null) { log("Hawk.get -> Decrypt failed"); return null }
        return try { converter.fromString<T>(plainText, info).also { log("Hawk.get -> Converted to : $it") } } catch (_: Exception) {
            log("Hawk.get -> Converter failed")
            null
        }
    }
    override fun <T> get(key: String?, defaultValue: T): T = get<T>(key) ?: defaultValue
    override fun count(): Long = storage.count()
    override fun deleteAll(): Boolean = storage.deleteAll()
    override fun delete(key: String?): Boolean = storage.delete(key)
    override fun contains(key: String?): Boolean = storage.contains(key)
    override fun isBuilt(): Boolean = true
    override fun destroy() = Unit
    private fun log(message: String) { logInterceptor.onLog(message) }
}
