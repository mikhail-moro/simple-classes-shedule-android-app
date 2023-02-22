package org.karasiksoftware.dataclasses.database

import org.karasiksoftware.utils.JWTUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DatabaseUserTokenData
(
    val token: String
) {
    val endDate = if (token != "") {
        val jwtUtils = JWTUtils()
        val timestamp = jwtUtils.getTimestamp(token)

        Instant.ofEpochSecond(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } else {
        null
    }

    val isTokenAvailable = if (endDate != null && token != "") LocalDate.now() < endDate else false
}