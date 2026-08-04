package com.orhanobut.hawk

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * [Storage] implementation backed by Preferences DataStore.
 *
 * Hawk keeps its synchronous API, while DataStore is asynchronous underneath. Each operation
 * bridges synchronously via [runBlocking]; the actual DataStore work is executed on DataStore's
 * internal IO dispatcher, so the caller only waits for the result. DataStore serialises writes
 * internally, so concurrent `put`/`delete` calls from multiple threads cannot lose updates.
 *
 * Exactly one DataStore instance is created per underlying file and retained for the lifetime of
 * the process (the [preferencesDataStore] delegate is a process-wide singleton per file name).
 */
class DataStoreStorage(context: Context) : Storage {

  companion object {
    /**
     * Never change: must match the legacy SharedPreferences file name ("Hawk2") used by Hawk 2.x,
     * so that one-time migration can read the old data from the same logical store.
     */
    const val DATASTORE_NAME = "Hawk2"
  }

  private val dataStore: DataStore<Preferences> = context.applicationContext.hawkNextDataStore

  /**
   * The underlying DataStore instance. Intended for internal use (legacy migration).
   */
  fun dataStore(): DataStore<Preferences> = dataStore

  override fun <T> put(key: String?, value: T): Boolean {
    HawkUtils.checkNull("key", key)
    return runCatching {
      runBlocking {
        dataStore.edit { preferences ->
          preferences[stringPreferencesKey(key!!)] = value.toString()
        }
      }
    }.isSuccess
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> get(key: String): T? {
    return runCatching {
      runBlocking {
        dataStore.data.first()[stringPreferencesKey(key)] as T?
      }
    }.getOrNull()
  }

  override fun delete(key: String?): Boolean {
    HawkUtils.checkNull("key", key)
    return runCatching {
      runBlocking {
        dataStore.edit { preferences ->
          preferences.remove(stringPreferencesKey(key!!))
        }
      }
    }.isSuccess
  }

  override fun deleteAll(): Boolean {
    return runCatching {
      runBlocking {
        dataStore.edit { preferences ->
          preferences.clear()
        }
      }
    }.isSuccess
  }

  override fun contains(key: String): Boolean {
    return runCatching {
      runBlocking {
        dataStore.data.first().contains(stringPreferencesKey(key))
      }
    }.getOrDefault(false)
  }

  override fun count(): Long {
    return runCatching {
      runBlocking {
        dataStore.data.first().asMap().size
      }.toLong()
    }.getOrDefault(0L)
  }

  override fun keys(): List<String> {
    return runCatching {
      runBlocking {
        dataStore.data.first().asMap().keys.map { it.name }
      }
    }.getOrDefault(emptyList())
  }
}

/**
 * Process-wide singleton: exactly one DataStore instance per file name.
 */
private val Context.hawkNextDataStore: DataStore<Preferences> by preferencesDataStore(
  name = DataStoreStorage.DATASTORE_NAME
)
