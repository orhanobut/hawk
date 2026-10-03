package com.orhanobut.hawk


/** Converts values using the type metadata stored by [Serializer]. */
interface Converter {
    fun <T> toString(value: T?): String?
    @Throws(Exception::class)
    fun <T> fromString(value: String?, dataInfo: DataInfo?): T?
}
