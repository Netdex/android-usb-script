package org.netdex.androidusbscript.util

object Util {
    private val HEX_ARRAY = "0123456789abcdef".toCharArray()
    fun escapeHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 4)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 4] = '\\'
            hexChars[j * 4 + 1] = 'x'
            hexChars[j * 4 + 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 4 + 3] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
}
