package com.fonrouge.fsLib.security

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val username: String? = null, val password: String? = null)
