package org.karasiksoftware.utils

import java.util.*

class JWTUtils {
    fun getTimestamp(token: String): Long {
        val parts = token.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val partAsBytes = parts[1].toByteArray(charset("UTF-8"))
        val decodedPart = String(Base64.getUrlDecoder().decode(partAsBytes))

        val rawData = decodedPart.slice(1 .. decodedPart.length-2).split(",".toRegex())

        rawData.forEach {
            val data = it.split(":".toRegex())
            if (data[0] == "\"exp\"") {
                return data[1].toLong()
            }
        }

        return 0L
    }
}