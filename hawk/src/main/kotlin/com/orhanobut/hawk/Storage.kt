package com.orhanobut.hawk


/** Persistence layer. Return false when a write cannot be committed. */
interface Storage {
    fun <T> put(key: String?, value: T?): Boolean
    fun <T> get(key: String?): T?
    fun delete(key: String?): Boolean
    fun deleteAll(): Boolean
    fun count(): Long
    fun contains(key: String?): Boolean
}
