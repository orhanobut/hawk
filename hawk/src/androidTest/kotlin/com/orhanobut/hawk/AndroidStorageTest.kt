package com.orhanobut.hawk

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AndroidStorageTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Before fun clear() { context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE).edit().clear().commit() }
    @After fun cleanup() { clear() }
    @Test fun preferencesSurviveReinitializationAndCommitDeletion() {
        Hawk.init(context).setEncryption(NoEncryption()).build()
        assertTrue(Hawk.put("unicode", "héllo 🦅"))
        assertTrue(Hawk.put("list", listOf("foo", "bar")))
        Hawk.init(context).setEncryption(NoEncryption()).build()
        assertEquals("héllo 🦅", Hawk.get<String>("unicode"))
        assertEquals(listOf("foo", "bar"), Hawk.get<List<String>>("list"))
        assertEquals(2L, Hawk.count())
        assertTrue(Hawk.delete("unicode"))
        assertFalse(Hawk.contains("unicode"))
        assertTrue(Hawk.deleteAll())
        assertEquals(0L, Hawk.count())
    }
    @Test fun noEncryptionRetainsAndroidBase64WireFormat() {
        val encryption = NoEncryption()
        assertTrue(encryption.init())
        assertEquals("aGF3aw==\n", encryption.encrypt("key", "hawk"))
        for (value in listOf("", "héllo 🦅", "x".repeat(200))) {
            assertEquals(value, encryption.decrypt("key", encryption.encrypt("key", value)))
        }
        assertEquals("hawk", encryption.decrypt("key", "aGF3aw=="))
    }
    @Test fun defaultKeystoreEncryptsAndReadsAcrossRebuilds() {
        val encryption = KeystoreEncryption()
        assertTrue("Android Keystore must be available", encryption.init())
        val ciphertext = encryption.encrypt("key", "héllo 🦅")
        assertEquals("héllo 🦅", KeystoreEncryption().decrypt("key", ciphertext))
        assertThrows(Exception::class.java) { encryption.decrypt("different-key", ciphertext) }
        Hawk.init(context).build()
        assertTrue(Hawk.put("secret", "saved"))
        Hawk.init(context).build()
        assertEquals("saved", Hawk.get<String>("secret"))
    }
    @Test fun builderUsesSuppliedLayersAndNullSettersRestoreDefaults() {
        val preferences = context.getSharedPreferences("hawk-test-custom", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val storage = SharedPreferencesStorage(preferences)
        val logs = mutableListOf<String>()
        val builder = HawkBuilder(context).setStorage(storage).setEncryption(NoEncryption())
            .setLogInterceptor { logs += it }
        builder.build()
        assertTrue(Hawk.put("custom", "value"))
        assertTrue(preferences.contains("custom"))
        assertTrue(logs.isNotEmpty())
        builder.setStorage(null).setParser(null).setSerializer(null).setConverter(null).setLogInterceptor(null).build()
        assertTrue(Hawk.put("default", "value"))
        assertTrue(context.getSharedPreferences("Hawk2", Context.MODE_PRIVATE).contains("default"))
        assertFalse(preferences.contains("default"))
        preferences.edit().clear().commit()
    }
    @Test fun initRequiresBuildBeforeOperations() {
        Hawk.init(context)
        assertFalse(Hawk.isBuilt())
        assertThrows(IllegalStateException::class.java) { Hawk.put("key", "value") }
        Hawk.init(context).setEncryption(NoEncryption()).build()
        assertTrue(Hawk.isBuilt())
    }
}
