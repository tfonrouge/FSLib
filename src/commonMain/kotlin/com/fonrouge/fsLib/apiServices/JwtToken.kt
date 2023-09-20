package com.fonrouge.fsLib.apiServices

import kotlinx.serialization.Serializable

@Serializable
data class JwtToken(
    val token: String
)
