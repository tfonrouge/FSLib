package com.fonrouge.fsLib.apiServices

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(
    val username: String,
    val password: String,
)
