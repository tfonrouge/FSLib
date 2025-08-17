package com.fonrouge.base.security

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val username: String? = null, val password: String? = null)
