package com.orhanobut.hawk

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets

/**
 * One-time migration of Hawk 2.x data from the legacy "Hawk2" SharedPreferences file into
 * Preferences DataStore.
 *
 * Guarantees:
 *  - Migration runs at most once (tracked by a flag in a separate "Hawk2Migration" prefs file).
 *  - All entries are written in a single transactional DataStore [edit] - atomic from the user's
 *    perspective.
 *  - The legacy file is cleared only after the migration has completed successfully.
 *  - If anything fails, the legacy data is left untouched so the next launch retries.
 *
 * Values written by Hawk 2.x:
 *  - with NoEncryption: the plain text (Gson JSON) Base64 encoded - it is decoded and re-encrypted.
 *  - with Conceal 1.1.3: the payload is decrypted with the documented legacy AES-GCM format
 *    ([LegacyConcealDecryptor]) and re-encrypted with the current [Encryption].
 *  - entries that cannot be interpreted (corrupted or undecryptable encrypted values) are copied
 *    verbatim so no data is lost; those entries return null from `Hawk.get` until the value is
 *    re-saved. This is documented in docs/migration.md.
 */
internal class LegacyMigrator @JvmOverloads constructor(
  private val context: Context,
  private val logInterceptor: LogInterceptor,
  private val encryption: Encryption,
  private val legacyConcealDecryptor: LegacyConcealDecryptor = LegacyConcealDecryptor(context)
) {

  companion object {
    const val LEGACY_PREFS_NAME = "Hawk2"
    const val MIGRATION_FLAG_PREFS_NAME = "Hawk2Migration"
    const val MIGRATION_FLAG_KEY = "migrated"
  }

  /**
   * @return true when migration finished successfully or was already done
   */
  fun migrate(dataStore: DataStore<Preferences>): Boolean {
    val flagPrefs = context.getSharedPreferences(MIGRATION_FLAG_PREFS_NAME, Context.MODE_PRIVATE)
    if (flagPrefs.getBoolean(MIGRATION_FLAG_KEY, false)) {
      return true
    }

    val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    val legacyEntries = legacyPrefs.all.filterValues { it is String }.mapValues { it.value as String }

    return try {
      if (legacyEntries.isNotEmpty()) {
        runBlocking {
          dataStore.edit { preferences ->
            legacyEntries.forEach { (key, serialized) ->
              preferences[stringPreferencesKey(key)] = migrateEntry(key, serialized)
            }
          }
        }
      }

      val flagCommitted = flagPrefs.edit().putBoolean(MIGRATION_FLAG_KEY, true).commit()
      if (flagCommitted) {
        legacyPrefs.edit().clear().commit()
      }
      logInterceptor.onLog(
        "Hawk migration: migrated ${legacyEntries.size} legacy entr${if (legacyEntries.size == 1) "y" else "ies"} from SharedPreferences to DataStore."
      )
      flagCommitted
    } catch (e: Exception) {
      logInterceptor.onLog("Hawk migration failed: ${e.message}. Legacy data was preserved.")
      false
    }
  }

  /**
   * Returns the serialized text that should be stored for a legacy entry:
   * re-encrypted when possible, copied verbatim otherwise.
   */
  private fun migrateEntry(key: String, serialized: String): String {
    val parts = serialized.split('#')
    if (parts.size < 3) {
      return serialized
    }
    val typeAndPayload = parts[2]
    val delimiterIndex = typeAndPayload.indexOf('@')
    if (delimiterIndex < 0) {
      return serialized
    }
    val metadataPrefix = "${parts[0]}#${parts[1]}#${typeAndPayload.substring(0, delimiterIndex)}"
    val legacyCipherText = typeAndPayload.substring(delimiterIndex + 1)

    val plainText = legacyConcealDecryptor.tryDecrypt(key, legacyCipherText)
        ?: tryDecryptAsLegacyPlainText(legacyCipherText)

    if (plainText == null) {
      val reason = if (legacyConcealDecryptor.looksLikeConceal(legacyCipherText)) {
        "encrypted with Conceal but could not be decrypted"
      } else {
        "corrupted or not readable"
      }
      logInterceptor.onLog(
        "Hawk migration: entry '$key' was $reason and was copied as-is. Re-save the value to make it readable."
      )
      return serialized
    }

    return try {
      val newCipherText = encryption.encrypt(key, plainText)
      "$metadataPrefix@$newCipherText"
    } catch (e: Exception) {
      logInterceptor.onLog("Hawk migration: re-encryption failed for '$key': ${e.message}")
      serialized
    }
  }

  /**
   * Hawk 2.x NoEncryption stored the plain text (Gson JSON) Base64 encoded with Base64.DEFAULT.
   * Conceal payloads start with the serialization version byte (1) and are handled above;
   * valid JSON never starts with a control byte, so this cannot misclassify encrypted data.
   */
  private fun tryDecryptAsLegacyPlainText(base64CipherText: String): String? {
    if (base64CipherText.isEmpty()) {
      return null
    }
    return try {
      val bytes = Base64.decode(base64CipherText, Base64.DEFAULT)
      if (bytes.isEmpty() || bytes[0].toInt() == LegacyConcealDecryptor.CIPHER_SERIALIZATION_VERSION.toInt()) {
        null
      } else {
        String(bytes, StandardCharsets.UTF_8)
      }
    } catch (e: Exception) {
      null
    }
  }
}
