package com.orhanobut.hawk


/** The Hawk 2 metadata header must stay compatible with existing preferences. */
internal open class HawkSerializer(private val logInterceptor: LogInterceptor) : Serializer {
    override fun <T> serialize(cipherText: String?, value: T?): String {
        HawkUtils.checkNullOrEmpty("Cipher text", cipherText)
        HawkUtils.checkNull("Value", value)
        var keyClassName = ""
        var valueClassName = ""
        val dataType = when (value) {
            is List<*> -> {
                if (value.isNotEmpty()) keyClassName = value.first()!!.javaClass.name
                DataInfo.TYPE_LIST
            }
            is Map<*, *> -> {
                if (value.isNotEmpty()) {
                    val entry = value.entries.first()
                    keyClassName = entry.key!!.javaClass.name
                    valueClassName = entry.value!!.javaClass.name
                }
                DataInfo.TYPE_MAP
            }
            is Set<*> -> {
                if (value.isNotEmpty()) keyClassName = value.first()!!.javaClass.name
                DataInfo.TYPE_SET
            }
            else -> { keyClassName = value!!.javaClass.name; DataInfo.TYPE_OBJECT }
        }
        return "$keyClassName#$valueClassName#${dataType}V@$cipherText"
    }
    override fun deserialize(plainText: String): DataInfo {
        val infos = plainText.split('#')
        val type = infos[2][0]
        val text = infos.last()
        val delimiter = text.indexOf('@')
        require(delimiter != -1) { "Text should contain delimiter" }
        return DataInfo(type, text.substring(delimiter + 1), loadClass(infos[0]), loadClass(infos[1]))
    }
    private fun loadClass(name: String): Class<*>? = if (name.isEmpty()) null else try {
        Class.forName(name)
    } catch (e: ClassNotFoundException) {
        logInterceptor.onLog("HawkSerializer -> ${e.message}")
        null
    }
}
