package com.orhanobut.hawk

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class PipelineTest {
    data class Model(val name: String, val number: Int)
    private val storage = MemoryStorage()
    private val logs = mutableListOf<String>()
    private val logger = LogInterceptor { logs += it }
    private val converter = HawkConverter(GsonParser(Gson()))
    private val serializer = HawkSerializer(logger)
    private val encryption = RecordingEncryption()
    private fun facade(
        converter: Converter = this.converter,
        encryption: Encryption = this.encryption,
        serializer: Serializer = this.serializer,
    ) = DefaultHawkFacade(storage, converter, encryption, serializer, logger)

    @Test fun roundTripsPrimitivesModelsAndCollections() {
        val values = listOf<Any>(true, "hawk", 1.5f, 10, 'A', Model("hawk", 7),
            listOf("foo", "bar"), setOf("foo", "bar"), mapOf("key" to "value"),
            listOf(Model("one", 1), Model("two", 2)), setOf(Model("one", 1)),
            mapOf("key" to Model("one", 1)), emptyList<String>(), emptySet<String>(), emptyMap<String, String>())
        val facade = facade()
        values.forEachIndexed { index, value ->
            assertTrue(facade.put(index.toString(), value))
            assertEquals(value, facade.get<Any>(index.toString()))
        }
        assertEquals(values.size.toLong(), facade.count())
        assertEquals("key", (facade.get<Map<String, Model>>("11")!!).keys.single())
    }
    @Test fun collectionNumbersRetainTheirRecordedTypes() {
        val facade = facade()
        assertTrue(facade.put("list", listOf(1, 2)))
        assertEquals(listOf(1, 2), facade.get<List<Int>>("list"))
        assertTrue(facade.put("map", mapOf(1 to 2L)))
        assertEquals(mapOf(1 to 2L), facade.get<Map<Int, Long>>("map"))
    }
    @Test fun readsLegacyHeaderWithoutRewritingIt() {
        storage.values["legacy"] = "java.lang.String##0V@encrypted:\"saved\""
        assertEquals("saved", facade().get<String>("legacy"))
        assertEquals("java.lang.String##0V@encrypted:\"saved\"", storage.values["legacy"])
    }
    @Test fun nullValueDeletesAndDefaultsHandleMissingOrNullKeys() {
        val facade = facade()
        assertTrue(facade.put("key", "value"))
        assertTrue(facade.contains("key"))
        assertTrue(facade.put<String>("key", null))
        assertFalse(facade.contains("key"))
        assertNull(facade.get<String>("key"))
        assertNull(facade.get<String>(null))
        assertEquals("default", facade.get(null, "default"))
        assertEquals("default", facade.get("missing", "default"))
        assertNull(facade.get<String?>("missing", null))
    }
    @Test fun nullKeyFailsBeforeAnyWrite() {
        val exception = assertThrows(NullPointerException::class.java) { facade().put(null, "value") }
        assertEquals("Key should not be null", exception.message)
        assertTrue(storage.values.isEmpty())
    }
    @Test fun deleteAndClearPersistObservableResults() {
        val facade = facade()
        repeat(100) { assertTrue(facade.put(it.toString(), it)) }
        repeat(100) { assertEquals(it, facade.get<Int>(it.toString())) }
        assertEquals(100L, facade.count())
        assertTrue(facade.delete("0"))
        assertEquals(99L, facade.count())
        assertTrue(facade.deleteAll())
        assertEquals(0L, facade.count())
    }
    @Test fun conversionFailureDoesNotOverwriteExistingData() {
        storage.values["key"] = "existing"
        val failing = object : Converter by converter {
            override fun <T> toString(value: T?): String? = null
        }
        assertFalse(facade(converter = failing).put("key", "value"))
        assertEquals("existing", storage.values["key"])
        assertEquals(0, encryption.encryptCalls)
    }
    @Test fun encryptionNullAndExceptionDoNotWrite() {
        for (throws in listOf(false, true)) {
            val failing = object : Encryption by encryption {
                override fun encrypt(key: String, value: String): String? {
                    if (throws) throw Exception("encryption failed")
                    return null
                }
            }
            assertFalse(facade(encryption = failing).put("key", "value"))
            assertTrue(storage.values.isEmpty())
        }
    }
    @Test fun serializationFailureDoesNotWrite() {
        val failing = object : Serializer by serializer {
            override fun <T> serialize(cipherText: String?, value: T?): String? = null
        }
        assertFalse(facade(serializer = failing).put("key", "value"))
        assertTrue(storage.values.isEmpty())
    }
    @Test fun failedStorageCommitIsReported() {
        storage.allowWrites = false
        assertFalse(facade().put("key", "value"))
        assertTrue(storage.values.isEmpty())
        assertFalse(facade().delete("key"))
        assertFalse(facade().deleteAll())
    }
    @Test fun missingStorageAndNullMetadataReturnNullWithoutDecrypting() {
        assertNull(facade().get<Any>("missing"))
        storage.values["key"] = "serialized"
        val failing = object : Serializer by serializer {
            override fun deserialize(plainText: String): DataInfo? = null
        }
        assertNull(facade(serializer = failing).get<Any>("key"))
        assertEquals(0, encryption.decryptCalls)
    }
    @Test fun decryptNullAndExceptionReturnDefault() {
        assertTrue(facade().put("key", "value"))
        for (throws in listOf(false, true)) {
            val failing = object : Encryption by encryption {
                override fun decrypt(key: String, value: String): String? {
                    if (throws) throw Exception("decrypt failed")
                    return null
                }
            }
            assertEquals("default", facade(encryption = failing).get("key", "default"))
        }
    }
    @Test fun converterNullAndExceptionReturnDefault() {
        assertTrue(facade().put("key", "value"))
        for (throws in listOf(false, true)) {
            val failing = object : Converter by converter {
                override fun <T> fromString(value: String?, dataInfo: DataInfo?): T? {
                    if (throws) throw Exception("decode failed")
                    return null
                }
            }
            assertEquals("default", facade(converter = failing).get("key", "default"))
        }
    }
    @Test fun malformedMetadataKeepsExplicitFailureBehavior() {
        storage.values["key"] = "not a header"
        assertThrows(IndexOutOfBoundsException::class.java) { facade().get<Any>("key") }
    }
    @Test fun unknownClassesAreLoggedAndUnknownTypesReturnNull() {
        val info = serializer.deserialize("missing.Class##9V@cipher")
        assertNull(info.keyClazz)
        assertTrue(logs.any { it.contains("missing.Class") })
        assertNull(converter.fromString<Any>("{}", info))
    }
    @Test fun emptyAndWhitespaceJsonAndUnicodeTrimBoundaries() {
        val parser = GsonParser(Gson())
        assertNull(parser.fromJson<String>(null, String::class.java))
        assertNull(parser.fromJson<String>("", String::class.java))
        assertNull(parser.fromJson<String>("   ", String::class.java))
        assertFalse(HawkUtils.isEmpty("\u2003"))
        assertEquals("null", parser.toJson(null))
    }
    @Test fun serializationRejectsNullAndPreservesFirstElementLimitations() {
        assertThrows(NullPointerException::class.java) { serializer.serialize(" ", "value") }
        assertThrows(NullPointerException::class.java) { serializer.serialize<String>("cipher", null) }
        assertThrows(NullPointerException::class.java) { serializer.serialize("cipher", listOf(null)) }
        assertEquals("##1V@cipher", serializer.serialize("cipher", emptyList<String>()))
    }
}

internal class MemoryStorage : Storage {
    val values = linkedMapOf<String?, Any?>()
    var allowWrites = true
    override fun <T> put(key: String?, value: T?): Boolean {
        if (!allowWrites) return false
        values[key] = value
        return true
    }
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String?): T? = values[key] as T?
    override fun delete(key: String?): Boolean {
        if (!allowWrites) return false
        values.remove(key)
        return true
    }
    override fun deleteAll(): Boolean {
        if (!allowWrites) return false
        values.clear()
        return true
    }
    override fun count(): Long = values.size.toLong()
    override fun contains(key: String?): Boolean = values.containsKey(key)
}

private class RecordingEncryption : Encryption {
    var encryptCalls = 0
    var decryptCalls = 0
    override fun init(): Boolean = true
    override fun encrypt(key: String, value: String): String { encryptCalls++; return "encrypted:$value" }
    override fun decrypt(key: String, value: String): String { decryptCalls++; return value.removePrefix("encrypted:") }
}
