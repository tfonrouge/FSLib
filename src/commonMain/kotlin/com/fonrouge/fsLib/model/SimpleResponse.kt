package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse(
    var isOk: Boolean,
    var msgOk: String? = null,
    var msgError: String? = null,
    var data: String? = null
)
