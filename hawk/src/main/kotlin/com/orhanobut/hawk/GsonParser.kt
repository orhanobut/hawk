package com.orhanobut.hawk


import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

/** Default JSON parser. Configure Gson adapters for custom model types. */
class GsonParser(private val gson: Gson) : Parser {
    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(content: String?, type: Type?): T? =
        if (content.isNullOrEmpty()) null else gson.fromJson(content, type)
    override fun toJson(body: Any?): String = gson.toJson(body)
}
