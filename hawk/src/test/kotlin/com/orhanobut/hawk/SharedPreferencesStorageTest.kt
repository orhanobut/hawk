package com.orhanobut.hawk

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test

class SharedPreferencesStorageTest {
    @Test fun commitsWritesAndReportsCommitFailure() {
        val preferences = RecordingPreferences()
        val storage = SharedPreferencesStorage(preferences)
        assertTrue(storage.put("key", 42))
        assertEquals("42", storage.get<String>("key"))
        assertTrue(storage.contains("key"))
        assertEquals(1L, storage.count())
        assertEquals(1, preferences.commits)
        preferences.allowCommit = false
        assertFalse(storage.put("key", "replacement"))
        assertEquals("42", storage.get<String>("key"))
        assertEquals(2, preferences.commits)
    }
    @Test fun failedDeleteAndClearLeaveExistingValuesIntact() {
        val preferences = RecordingPreferences()
        val storage = SharedPreferencesStorage(preferences)
        assertTrue(storage.put("first", "value"))
        assertTrue(storage.put("second", "other"))
        preferences.allowCommit = false
        assertFalse(storage.delete("first"))
        assertFalse(storage.deleteAll())
        assertEquals(2L, storage.count())
        preferences.allowCommit = true
        assertTrue(storage.delete("first"))
        assertNull(storage.get<String>("first"))
        assertEquals(1L, storage.count())
        assertTrue(storage.deleteAll())
        assertEquals(0L, storage.count())
    }
    @Test fun nullKeyFailsBeforeEditingAndNullValueUsesLegacyStringRepresentation() {
        val preferences = RecordingPreferences()
        val storage = SharedPreferencesStorage(preferences)
        assertEquals("key should not be null", assertThrows(NullPointerException::class.java) {
            storage.put(null, "value")
        }.message)
        assertEquals(0, preferences.commits)
        assertTrue(storage.put<String>("key", null))
        assertEquals("null", storage.get<String>("key"))
    }
}

/** A recording fake of the platform interface; no Android constructors or method stubs run. */
private class RecordingPreferences : SharedPreferences {
    private val values = linkedMapOf<String?, String?>()
    var allowCommit = true
    var commits = 0
    override fun getAll(): MutableMap<String?, *> = LinkedHashMap(values)
    override fun getString(key: String?, defValue: String?): String? = values[key] ?: defValue
    override fun contains(key: String?): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val changes = linkedMapOf<String?, String?>()
        private var clear = false
        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply { changes[key] = value }
        override fun remove(key: String?): SharedPreferences.Editor = apply { changes[key] = null }
        override fun clear(): SharedPreferences.Editor = apply { clear = true }
        override fun commit(): Boolean {
            commits++
            if (!allowCommit) return false
            if (clear) values.clear()
            for ((key, value) in changes) {
                if (value == null) values.remove(key) else values[key] = value
            }
            return true
        }
        override fun apply(): Unit = error("Storage must synchronously report commit failures")
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = error("unused")
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = error("unused")
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = error("unused")
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = error("unused")
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = error("unused")
    }
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = error("unused")
    override fun getInt(key: String?, defValue: Int): Int = error("unused")
    override fun getLong(key: String?, defValue: Long): Long = error("unused")
    override fun getFloat(key: String?, defValue: Float): Float = error("unused")
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = error("unused")
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
}
