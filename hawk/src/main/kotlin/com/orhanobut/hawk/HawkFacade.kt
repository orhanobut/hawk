package com.orhanobut.hawk


/** Operations provided by the initialized storage pipeline. */
interface HawkFacade {
    fun <T> put(key: String?, value: T?): Boolean
    fun <T> get(key: String?): T?
    fun <T> get(key: String?, defaultValue: T): T
    fun count(): Long
    fun deleteAll(): Boolean
    fun delete(key: String?): Boolean
    fun contains(key: String?): Boolean
    fun isBuilt(): Boolean
    fun destroy()

    open class EmptyHawkFacade : HawkFacade {
        private fun throwValidation(): Nothing = throw IllegalStateException(
            "Hawk is not built. Please call build() and wait the initialisation finishes."
        )
        override fun <T> put(key: String?, value: T?): Boolean = throwValidation()
        override fun <T> get(key: String?): T? = throwValidation()
        override fun <T> get(key: String?, defaultValue: T): T = throwValidation()
        override fun count(): Long = throwValidation()
        override fun deleteAll(): Boolean = throwValidation()
        override fun delete(key: String?): Boolean = throwValidation()
        override fun contains(key: String?): Boolean = throwValidation()
        override fun isBuilt(): Boolean = false
        override fun destroy(): Unit = throwValidation()
    }
}
