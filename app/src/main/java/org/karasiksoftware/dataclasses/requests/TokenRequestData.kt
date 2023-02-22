package org.karasiksoftware.dataclasses.requests

import kotlinx.serialization.Serializable

@Serializable
data class TokenData(
    val accessToken: String? = null
)

@Serializable
data class TokenRequestData(
    private val data: TokenData? = null,
    val state: Int? = null
) {
    val token = if (state != -1) {
        data?.accessToken!!
    } else {
        ""
    }
}
