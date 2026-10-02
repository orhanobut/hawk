package com.orhanobut.hawk

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class HawkTest {
    @After fun reset() { Hawk.hawkFacade = HawkFacade.EmptyHawkFacade() }
    @Test fun staticEntryPointsDelegateToTheFacade() {
        val events = mutableListOf<String>()
        val recording = object : HawkFacade {
            override fun <T> put(key: String?, value: T?): Boolean { events += "put:$key:$value"; return true }
            override fun <T> get(key: String?): T? { events += "get:$key"; return null }
            override fun <T> get(key: String?, defaultValue: T): T { events += "default:$key"; return defaultValue }
            override fun count(): Long { events += "count"; return 7 }
            override fun deleteAll(): Boolean { events += "clear"; return true }
            override fun delete(key: String?): Boolean { events += "delete:$key"; return true }
            override fun contains(key: String?): Boolean { events += "contains:$key"; return true }
            override fun isBuilt(): Boolean { events += "built"; return true }
            override fun destroy() { events += "destroy" }
        }
        Hawk.hawkFacade = recording
        assertTrue(Hawk.put("key", "value"))
        assertNull(Hawk.get<Any>("key"))
        assertEquals("fallback", Hawk.get("key", "fallback"))
        assertEquals(7L, Hawk.count())
        assertTrue(Hawk.contains("key"))
        assertTrue(Hawk.delete("key"))
        assertTrue(Hawk.deleteAll())
        assertTrue(Hawk.isBuilt())
        Hawk.destroy()
        assertEquals(listOf("put:key:value", "get:key", "default:key", "count", "contains:key", "delete:key", "clear", "built", "destroy"), events)
    }
    @Test fun nullContextRetainsItsValidationMessage() {
        assertEquals("Context should not be null", assertThrows(NullPointerException::class.java) { Hawk.init(null) }.message)
        assertEquals("Context should not be null", assertThrows(NullPointerException::class.java) { HawkBuilder(null) }.message)
    }
}
