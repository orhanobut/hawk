package com.orhanobut.hawk


import android.content.Context

/** Simple key-value storage for Android. Initialize and build before using it. */
class Hawk private constructor() {
    companion object {
        @JvmField internal var hawkFacade: HawkFacade = HawkFacade.EmptyHawkFacade()
        @JvmStatic fun init(context: Context?): HawkBuilder {
            HawkUtils.checkNull("Context", context)
            hawkFacade = HawkFacade.EmptyHawkFacade()
            return HawkBuilder(context)
        }
        @JvmStatic @JvmName("build") internal fun build(builder: HawkBuilder) {
            hawkFacade = DefaultHawkFacade(builder)
        }
        /** A null value deletes the key. Writes synchronously commit to storage. */
        @JvmStatic fun <T> put(key: String?, value: T?): Boolean = hawkFacade.put(key, value)
        @JvmStatic fun <T> get(key: String?): T? = hawkFacade.get(key)
        @JvmStatic fun <T> get(key: String?, defaultValue: T): T = hawkFacade.get(key, defaultValue)
        @JvmStatic fun count(): Long = hawkFacade.count()
        /** Clears values, retaining encryption key material. */
        @JvmStatic fun deleteAll(): Boolean = hawkFacade.deleteAll()
        @JvmStatic fun delete(key: String?): Boolean = hawkFacade.delete(key)
        @JvmStatic fun contains(key: String?): Boolean = hawkFacade.contains(key)
        @JvmStatic fun isBuilt(): Boolean = hawkFacade.isBuilt()
        @JvmStatic fun destroy() = hawkFacade.destroy()
    }
}
