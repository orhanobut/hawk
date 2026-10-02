package com.orhanobut.hawk

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class SerializationTest {
    @Test fun readsAndWritesTheExistingMetadataFormat() {
        val converter = HawkConverter(GsonParser(Gson()))
        val serializer = HawkSerializer(LogInterceptor {})
        // Literal fixtures catch incompatible format changes even if encoder and decoder agree.
        val fixtures = mapOf(
            "java.lang.String##0V@\"saved\"" to "saved",
            "java.lang.Integer##1V@[1,2]" to listOf(1, 2),
            "java.lang.String#java.lang.Long#2V@{\"key\":2}" to mapOf("key" to 2L),
            "java.lang.String##3V@[\"saved\"]" to setOf("saved"),
            "##1V@[]" to emptyList<String>(),
            "##2V@{}" to emptyMap<String, String>(),
            "##3V@[]" to emptySet<String>(),
        )
        for ((stored, expected) in fixtures) {
            val info = serializer.deserialize(stored)
            assertEquals(expected, converter.fromString<Any>(info.cipherText, info))
            assertEquals(stored, serializer.serialize(converter.toString(expected), expected))
        }
    }
}
