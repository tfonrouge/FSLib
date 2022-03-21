package com.fonrouge.fsLib.services

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String? = null,
    val name: String? = null,
    val username: String? = null,
    val password: String? = null,
    val password2: String? = null
)
