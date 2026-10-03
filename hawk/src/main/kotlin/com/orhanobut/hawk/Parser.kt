package com.orhanobut.hawk


import java.lang.reflect.Type

/** Converts values to and from JSON. Custom parsers must retain the persisted format. */
interface Parser {
    @Throws(Exception::class)
    fun <T> fromJson(content: String?, type: Type?): T?
    fun toJson(body: Any?): String?
}
