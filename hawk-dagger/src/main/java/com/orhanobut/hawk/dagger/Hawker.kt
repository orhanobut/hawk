package com.orhanobut.hawk.dagger

import android.annotation.SuppressLint
import android.content.Context
import com.orhanobut.hawk.DefaultHawkFacade
import com.orhanobut.hawk.HawkBuilder
import com.orhanobut.hawk.HawkFacade
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("StaticFieldLeak")
@Singleton
class Hawker @Inject constructor(context: Context) {
    private var hawkFacade: HawkFacade = DefaultHawkFacade(HawkBuilder(context))

    fun <T> put(key: String, value: T): Boolean = hawkFacade.put(key, value) ?: false

    fun <T> get(key: String): T? = hawkFacade.get(key)

    fun <T> get(key: String, defaultValue: T): T =
            hawkFacade.get(key, defaultValue) ?: defaultValue

    fun count(): Long = hawkFacade.count() ?: 0

    fun deleteAll(): Boolean = hawkFacade.deleteAll() ?: false

    fun delete(key: String): Boolean = hawkFacade.delete(key) ?: false

    fun contains(key: String): Boolean = hawkFacade.contains(key) ?: false

    fun destroy() {
        hawkFacade.destroy()
    }
}
