package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse(
    val isOk: Boolean,
    val msgOk: String? = null,
    val msgError: String? = null,
    val data: String? = null
)
