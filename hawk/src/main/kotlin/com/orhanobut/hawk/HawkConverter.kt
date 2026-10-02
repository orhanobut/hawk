package com.orhanobut.hawk


import com.google.gson.reflect.TypeToken

internal class HawkConverter(parser: Parser?) : Converter {
    private val parser: Parser
    init {
        HawkUtils.checkNull("Parser", parser)
        this.parser = parser!!
    }
    override fun <T> toString(value: T?): String? = if (value == null) null else parser.toJson(value)
    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T> fromString(value: String?, dataInfo: DataInfo?): T? {
        if (value == null) return null
        HawkUtils.checkNull("data info", dataInfo)
        val info = dataInfo!!
        // Concrete Object arguments avoid Gson's rejection of captured type variables.
        return when (info.dataType) {
            DataInfo.TYPE_OBJECT -> parser.fromJson<T>(value, info.keyClazz)
            DataInfo.TYPE_LIST -> if (info.keyClazz == null) arrayListOf<Any?>() as T else {
                val list = parser.fromJson<List<Any?>>(value, object : TypeToken<List<Any?>>() {}.type)!!
                list.mapTo(ArrayList()) { convert(it, info.keyClazz) } as T
            }
            DataInfo.TYPE_SET -> if (info.keyClazz == null) hashSetOf<Any?>() as T else {
                val set = parser.fromJson<Set<Any?>>(value, object : TypeToken<Set<Any?>>() {}.type)!!
                set.mapTo(HashSet()) { convert(it, info.keyClazz) } as T
            }
            DataInfo.TYPE_MAP -> if (info.keyClazz == null || info.valueClazz == null) hashMapOf<Any?, Any?>() as T else {
                val map = parser.fromJson<Map<Any?, Any?>>(value, object : TypeToken<Map<Any?, Any?>>() {}.type)!!
                val result = HashMap<Any?, Any?>()
                for ((key, item) in map) result[convert(key, info.keyClazz)] = convert(item, info.valueClazz)
                result as T
            }
            else -> null
        }
    }
    private fun convert(value: Any?, type: Class<*>): Any? = parser.fromJson<Any>(parser.toJson(value), type)
}
