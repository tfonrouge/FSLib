package com.fonrouge.fsLib.commonServices

import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class UserLogin(
    val username: String,
    val password: String,
)
