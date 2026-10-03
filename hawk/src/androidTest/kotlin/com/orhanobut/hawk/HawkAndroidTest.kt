package com.orhanobut.hawk

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import java.security.KeyStore
import javax.crypto.KeyGenerator
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Real Hawk, SharedPreferences and Keystore integration; no test doubles. */
class HawkAndroidTest {
    data class Model(val name: String, val number: Int)
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val alias = "com.orhanobut.hawk.aes-gcm"
    private fun store() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    @Before fun reset() {
        assertTrue(context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE).edit().clear().commit())
        store().deleteEntry(alias)
        Hawk.hawkFacade = HawkFacade.EmptyHawkFacade()
    }
    @After fun cleanup() { reset() }

    @Test fun defaultEncryptionAndPreferencesSurviveRebuildingHawk() {
        Hawk.init(context)
        assertFalse(Hawk.isBuilt())
        assertThrows(IllegalStateException::class.java) { Hawk.put("key", "value") }
        Hawk.init(context).build()
        assertThrows(NullPointerException::class.java) { Hawk.put(null, "value") }
        assertEquals(0L, Hawk.count())
        assertEquals("default", Hawk.get("missing", "default"))
        assertEquals("default", Hawk.get(null, "default"))
        assertTrue(Hawk.put("secret", "héllo 🦅"))
        val preferences = context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE)
        assertTrue(preferences.getString("secret", null)!!.contains("hawk-aes-gcm:"))
        Hawk.init(context).build()
        assertEquals("héllo 🦅", Hawk.get<String>("secret"))
        assertTrue(Hawk.contains("secret"))
        assertEquals(1L, Hawk.count())
        assertTrue(Hawk.delete("secret"))
        Hawk.init(context).build()
        assertNull(Hawk.get<String>("secret"))
        assertTrue(Hawk.put("other", "value"))
        assertTrue(Hawk.put<String>("other", null))
        assertFalse(Hawk.contains("other"))
        assertTrue(Hawk.put("last", "value"))
        assertTrue(Hawk.deleteAll())
        Hawk.init(context).build()
        assertEquals(0L, Hawk.count())
        assertNotNull(store().getKey(alias, null))
    }

    @Test fun roundTripsSupportedModelsAndCollectionsThroughTheDefaultPipeline() {
        Hawk.init(context).build()
        val values = listOf<Any>(true, "héllo 🦅", 1.5f, 10, 'A', Model("hawk", 7),
            listOf(1, 2), setOf("foo", "bar"), mapOf(1 to 2L),
            listOf(Model("one", 1), Model("two", 2)), setOf(Model("one", 1)),
            mapOf("key" to Model("one", 1)), emptyList<String>(), emptySet<String>(), emptyMap<String, String>())
        values.forEachIndexed { index, value -> assertTrue(Hawk.put(index.toString(), value)) }
        Hawk.init(context).build()
        values.forEachIndexed { index, value -> assertEquals(value, Hawk.get<Any>(index.toString())) }
        assertEquals(values.size.toLong(), Hawk.count())
    }

    @Test fun unreadableValuesReturnDefaultsAndFailedEncryptionPreservesStoredData() {
        Hawk.init(context).build()
        assertTrue(Hawk.put("key", "saved"))
        val preferences = context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE)
        val stored = preferences.getString("key", null)!!
        // A real model-type change makes Gson conversion fail after successful decryption.
        assertTrue(preferences.edit().putString("key", stored.replaceFirst("java.lang.String", "java.lang.Integer")).commit())
        assertEquals(-1, Hawk.get("key", -1))
        assertTrue(preferences.edit().putString("key", stored).commit())
        store().deleteEntry(alias)
        assertNull(Hawk.get<String>("key"))
        assertEquals("default", Hawk.get("key", "default"))
        assertFalse(store().containsAlias(alias))
        createIncompatibleKey()
        assertFalse(Hawk.put("key", "replacement"))
        assertEquals(stored, preferences.getString("key", null))
    }

    @Test fun keystoreKeyIsNonExportableAndRejectsTamperingAndKeyLoss() {
        val encryption = KeystoreEncryption(alias)
        assertTrue(encryption.init())
        val encrypted = encryption.encrypt("key", "héllo 🦅")
        assertNull(store().getKey(alias, null).encoded)
        assertEquals("héllo 🦅", KeystoreEncryption(alias).decrypt("key", encrypted))
        assertNotEquals(encrypted, encryption.encrypt("key", "héllo 🦅"))
        assertThrows(Exception::class.java) { encryption.decrypt("wrong-key", encrypted) }
        val prefix = "hawk-aes-gcm:"
        val bytes = Base64.decode(encrypted.removePrefix(prefix), Base64.NO_WRAP)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) {
            encryption.decrypt("key", prefix + Base64.encodeToString(bytes, Base64.NO_WRAP))
        }
        for (malformed in listOf("aGF3aw==", "hawk-aes-gcm:!", "hawk-aes-gcm:AA==")) {
            assertThrows(IllegalArgumentException::class.java) { encryption.decrypt("key", malformed) }
        }
        store().deleteEntry(alias)
        assertThrows(Exception::class.java) { encryption.decrypt("key", encrypted) }
        assertFalse(store().containsAlias(alias))
    }

    @Test fun unavailableDefaultKeyFailsInitializationWithoutPlaintextFallback() {
        createIncompatibleKey()
        assertThrows(IllegalStateException::class.java) { Hawk.init(context).build() }
        assertFalse(Hawk.isBuilt())
        assertThrows(IllegalStateException::class.java) { Hawk.put("secret", "value") }
        assertTrue(context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE).all.isEmpty())
    }

    @Test fun explicitlySelectedNoEncryptionReadsLegacyBase64Values() {
        val encryption = NoEncryption()
        assertEquals("aGF3aw==\n", encryption.encrypt("key", "hawk"))
        assertEquals("hawk", encryption.decrypt("key", "aGF3aw=="))
        context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE).edit()
            .putString("legacy", "java.lang.String##0V@aGF3aw==\n").commit()
        Hawk.init(context).setEncryption(encryption).build()
        assertEquals("hawk", Hawk.get<String>("legacy"))
        assertTrue(Hawk.put("unicode", "héllo 🦅"))
        Hawk.init(context).setEncryption(NoEncryption()).build()
        assertEquals("héllo 🦅", Hawk.get<String>("unicode"))
    }

    private fun createIncompatibleKey() {
        KeyGenerator.getInstance("HmacSHA256", "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN).setKeySize(256).build())
        }.generateKey()
    }
}
