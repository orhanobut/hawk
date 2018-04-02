package com.orhanobut.hawk

import android.content.SharedPreferences

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import java.util.HashMap

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@RunWith(RobolectricTestRunner::class)
class SharedPreferencesStorageTest {

  private lateinit var storage: SharedPreferencesStorage
  private lateinit var editor: SharedPreferences.Editor
  private lateinit var preferences: SharedPreferences

  @Before fun setup() {
    editor = spy<SharedPreferences.Editor>(object : SharedPreferences.Editor {
      override fun putString(key: String, value: String?): SharedPreferences.Editor {
        return this
      }

      override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
        return this
      }

      override fun putInt(key: String, value: Int): SharedPreferences.Editor {
        return this
      }

      override fun putLong(key: String, value: Long): SharedPreferences.Editor {
        return this
      }

      override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
        return this
      }

      override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
        return this
      }

      override fun remove(key: String): SharedPreferences.Editor {
        return this
      }

      override fun clear(): SharedPreferences.Editor {
        return this
      }

      override fun commit(): Boolean {
        return true
      }

      override fun apply() {}
    })
    preferences = spy<SharedPreferences>(object : SharedPreferences {
      override fun getAll(): Map<String, *> {
        val map = HashMap<String, Any>()
        map["key"] = "value"
        return map
      }

      override fun getString(key: String, defValue: String?): String? {
        return null
      }

      override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return null
      }

      override fun getInt(key: String, defValue: Int): Int {
        return 0
      }

      override fun getLong(key: String, defValue: Long): Long {
        return 0
      }

      override fun getFloat(key: String, defValue: Float): Float {
        return 0f
      }

      override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return false
      }

      override fun contains(key: String): Boolean {
        return false
      }

      override fun edit(): SharedPreferences.Editor? {
        return editor
      }

      override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {

      }

      override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {

      }
    })
    storage = SharedPreferencesStorage(preferences)
  }

  @Test fun putObject() {
    storage.put("key", "value")

    verify(preferences).edit()
    verify(editor).putString("key", "value")
    verify(editor).commit()
  }

  @Test fun throwExceptionOnNullKeysPassedOnPut() {
    try {
      storage.put(null, "value")
      fail("key should not be null")
    } catch (e: Exception) {
      assertThat(e).hasMessage("key should not be null")
    }

  }

  @Test fun get() {
    storage.get<Any>("key")

    verify(preferences).getString("key", null)
  }

  @Test fun remove() {
    storage.delete("key")

    verify(preferences).edit()
    verify(editor).remove("key")
    verify(editor).commit()
  }

  @Test fun contains() {
    storage.contains("key")

    verify(preferences).contains("key")
  }

  @Test fun deleteAll() {
    storage.deleteAll()

    verify(preferences).edit()
    verify(editor).clear()
    verify(editor).commit()
  }

  @Test fun count() {
    val count = storage.count()

    verify(preferences).all
    assertThat(count).isEqualTo(1)
  }
}