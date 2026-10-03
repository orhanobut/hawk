package com.orhanobut.hawk


/** Persisted type metadata, exposed for custom [Converter] and [Serializer] implementations. */
class DataInfo(
    @JvmField val dataType: Char,
    @JvmField val cipherText: String,
    @JvmField val keyClazz: Class<*>?,
    @JvmField val valueClazz: Class<*>?,
) {
    companion object {
        const val TYPE_OBJECT = '0'
        const val TYPE_LIST = '1'
        const val TYPE_MAP = '2'
        const val TYPE_SET = '3'
    }
}
