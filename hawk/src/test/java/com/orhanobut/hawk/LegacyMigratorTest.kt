package com.orhanobut.hawk

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.IOException
import javax.crypto.KeyGenerator
import javax.crypto.Cipher
import javax.crypto.SecretKey

@RunWith(RobolectricTestRunner::class)
class LegacyMigratorTest {

  private lateinit var context: Context
  private lateinit var storage: DataStoreStorage
  private lateinit var facade: HawkFacade
  private lateinit var encryption: Encryption
  private val gson = Gson()
  private val logs = StringBuilder()

  @Before fun setup() {
    context = RuntimeEnvironment.application
    encryption = KeystoreAesGcmEncryption("test-alias", newAesKey())
    val builder = HawkBuilder(context)
        .setLegacyMigrationEnabled(false)
        .setEncryption(encryption)
        .setLogInterceptor(LogInterceptor { logs.append(it).append('\n') })
    storage = builder.getStorage() as DataStoreStorage
    facade = DefaultHawkFacade(builder)
    storage.deleteAll()
    legacyPrefs().edit().clear().commit()
    flagPrefs().edit().clear().commit()
    cryptoPrefs().edit().clear().commit()
  }

  private fun migrator() = LegacyMigrator(context, LogInterceptor { logs.append(it).append('\n') }, encryption)

  private fun legacyPrefs(): SharedPreferences =
    context.getSharedPreferences(LegacyMigrator.LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

  private fun flagPrefs(): SharedPreferences =
    context.getSharedPreferences(LegacyMigrator.MIGRATION_FLAG_PREFS_NAME, Context.MODE_PRIVATE)

  private fun cryptoPrefs(): SharedPreferences =
    context.getSharedPreferences(LegacyConcealDecryptor.SHARED_PREF_NAME_256, Context.MODE_PRIVATE)

  private fun legacySerialized(keyClass: String, valueClass: String, type: Char, payloadBase64: String): String =
    "$keyClass#$valueClass#$type" + "V@" + payloadBase64

  private fun plainTextPayload(value: Any): String =
    Base64.encodeToString(gson.toJson(value).toByteArray(Charsets.UTF_8), Base64.DEFAULT)

  private fun concealPayload(entity: String, plainText: String, key: SecretKey): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv
    cipher.updateAAD(
      byteArrayOf(
        LegacyConcealDecryptor.CIPHER_SERIALIZATION_VERSION,
        LegacyConcealDecryptor.CIPHER_ID_256
      )
    )
    cipher.updateAAD(entity.toByteArray(Charsets.UTF_8))
    val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    val payload = ByteArray(2 + iv.size + cipherText.size)
    payload[0] = LegacyConcealDecryptor.CIPHER_SERIALIZATION_VERSION
    payload[1] = LegacyConcealDecryptor.CIPHER_ID_256
    System.arraycopy(iv, 0, payload, 2, iv.size)
    System.arraycopy(cipherText, 0, payload, 2 + iv.size, cipherText.size)
    return Base64.encodeToString(payload, Base64.NO_WRAP)
  }

  private fun newLegacyKey(): SecretKey =
    KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

  private fun newAesKey(): SecretKey =
    KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

  @Test fun emptyLegacyDatabaseMarksMigrationAsDone() {
    assertThat(migrator().migrate(storage.dataStore())).isTrue()
    assertThat(storage.count()).isEqualTo(0)
    assertThat(flagPrefs().getBoolean(LegacyMigrator.MIGRATION_FLAG_KEY, false)).isTrue()
  }

  @Test fun migratesPlainTextValues() {
    legacyPrefs().edit()
        .putString("string", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("Jack")))
        .putString("int", legacySerialized("java.lang.Integer", "", DataInfo.TYPE_OBJECT, plainTextPayload(30)))
        .putString("bool", legacySerialized("java.lang.Boolean", "", DataInfo.TYPE_OBJECT, plainTextPayload(true)))
        .putString("long", legacySerialized("java.lang.Long", "", DataInfo.TYPE_OBJECT, plainTextPayload(42L)))
        .putString("float", legacySerialized("java.lang.Float", "", DataInfo.TYPE_OBJECT, plainTextPayload(1.5f)))
        .putString("double", legacySerialized("java.lang.Double", "", DataInfo.TYPE_OBJECT, plainTextPayload(2.5)))
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(facade.get<String>("string")).isEqualTo("Jack")
    assertThat(facade.get<Int>("int")).isEqualTo(30)
    assertThat(facade.get<Boolean>("bool")).isEqualTo(true)
    assertThat(facade.get<Long>("long")).isEqualTo(42L)
    assertThat(facade.get<Float>("float")).isEqualTo(1.5f)
    assertThat(facade.get<Double>("double")).isEqualTo(2.5)
  }

  @Test fun migratesCustomObjectAndCollections() {
    legacyPrefs().edit()
        .putString("user", legacySerialized(FooBar::class.java.name, "", DataInfo.TYPE_OBJECT, plainTextPayload(FooBar())))
        .putString("list", legacySerialized("java.lang.String", "", DataInfo.TYPE_LIST, plainTextPayload(listOf("foo", "bar"))))
        .putString("map", legacySerialized("java.lang.String", "java.lang.String", DataInfo.TYPE_MAP, plainTextPayload(mapOf("key" to "value"))))
        .putString("emptyList", legacySerialized("", "", DataInfo.TYPE_LIST, plainTextPayload(listOf<String>())))
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    val user = facade.get<FooBar>("user")
    assertThat(user).isNotNull()
    assertThat(user!!.name).isEqualTo("hawk")
    assertThat(facade.get<List<String>>("list")).containsExactly("foo", "bar")
    assertThat(facade.get<Map<String, String>>("map")).containsEntry("key", "value")
    assertThat(facade.get<List<String>>("emptyList")).isNotNull()
  }

  @Test fun migratesConcealEncryptedValues() {
    val secretKey = newLegacyKey()
    cryptoPrefs().edit()
        .putString(
          LegacyConcealDecryptor.CIPHER_KEY_PREF,
          Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        )
        .commit()

    legacyPrefs().edit()
        .putString("secret", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, concealPayload("secret", gson.toJson("top-secret"), secretKey)))
        .putString("user", legacySerialized(FooBar::class.java.name, "", DataInfo.TYPE_OBJECT, concealPayload("user", gson.toJson(FooBar()), secretKey)))
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(facade.get<String>("secret")).isEqualTo("top-secret")
    val user = facade.get<FooBar>("user")
    assertThat(user).isNotNull()
    assertThat(user!!.name).isEqualTo("hawk")
  }

  @Test fun encryptedEntryThatCannotBeDecryptedIsPreservedVerbatim() {
    val payload = concealPayload("corrupt", gson.toJson("value"), newLegacyKey())
    val original = legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, payload)
    // Note: the cipher key is never stored in the legacy crypto prefs, so decryption must fail.
    legacyPrefs().edit()
        .putString("corrupt", original)
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(storage.contains("corrupt")).isTrue()
    assertThat(storage.get<String>("corrupt")).isEqualTo(original)
    assertThat(facade.get<String>("corrupt")).isNull()
    assertThat(logs.toString()).contains("copied as-is")
  }

  @Test fun migrationRunsOnlyOnce() {
    legacyPrefs().edit()
        .putString("first", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("one")))
        .commit()
    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    legacyPrefs().edit()
        .putString("second", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("two")))
        .commit()
    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(storage.contains("first")).isTrue()
    assertThat(storage.contains("second")).isFalse()
  }

  @Test fun legacyFileIsClearedAfterSuccessfulMigration() {
    legacyPrefs().edit()
        .putString("a", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("1")))
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(legacyPrefs().all).isEmpty()
    assertThat(flagPrefs().getBoolean(LegacyMigrator.MIGRATION_FLAG_KEY, false)).isTrue()
  }

  @Test fun nonStringLegacyValuesAreIgnored() {
    legacyPrefs().edit()
        .putString("str", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("value")))
        .putInt("num", 42)
        .commit()

    assertThat(migrator().migrate(storage.dataStore())).isTrue()

    assertThat(storage.contains("str")).isTrue()
    assertThat(storage.contains("num")).isFalse()
  }

  @Test fun migrationFailurePreservesLegacyData() {
    legacyPrefs().edit()
        .putString("keep", legacySerialized("java.lang.String", "", DataInfo.TYPE_OBJECT, plainTextPayload("value")))
        .commit()

    val failingDataStore = object : DataStore<Preferences> {
      override val data: Flow<Preferences> = flow { throw IOException("disk full") }
      override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
          throw IOException("disk full")
    }

    assertThat(migrator().migrate(failingDataStore)).isFalse()
    assertThat(flagPrefs().getBoolean(LegacyMigrator.MIGRATION_FLAG_KEY, false)).isFalse()
    assertThat(legacyPrefs().getString("keep", null)).isNotNull()
    assertThat(storage.contains("keep")).isFalse()
  }
}
