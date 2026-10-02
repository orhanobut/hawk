package com.orhanobut.hawk


import android.content.Context
import android.content.SharedPreferences

internal class SharedPreferencesStorage(private val preferences: SharedPreferences) : Storage {
    constructor(context: Context, tag: String) : this(context.getSharedPreferences(tag, Context.MODE_PRIVATE))
    override fun <T> put(key: String?, value: T?): Boolean {
        HawkUtils.checkNull("key", key)
        return preferences.edit().putString(key, value.toString()).commit()
    }
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String?): T? = preferences.getString(key, null) as T?
    override fun delete(key: String?): Boolean = preferences.edit().remove(key).commit()
    override fun contains(key: String?): Boolean = preferences.contains(key)
    override fun deleteAll(): Boolean = preferences.edit().clear().commit()
    override fun count(): Long = preferences.all.size.toLong()
}
