package com.orhanobut.hawk


internal object HawkUtils {
    @JvmStatic fun checkNull(message: String, value: Any?) {
        if (value == null) throw NullPointerException("$message should not be null")
    }
    @JvmStatic fun checkNullOrEmpty(message: String, value: String?) {
        if (isEmpty(value)) throw NullPointerException("$message should not be null or empty")
    }
    // Preserve Java String.trim(): only characters <= U+0020 are removed.
    @Suppress("TrimLambda")
    @JvmStatic fun isEmpty(text: String?): Boolean = text == null || text.trim { it <= ' ' }.isEmpty()
}
