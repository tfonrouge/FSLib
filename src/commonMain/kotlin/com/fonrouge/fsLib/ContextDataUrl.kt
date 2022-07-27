package com.fonrouge.fsLib

import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
class ContextDataUrl(
    val contextClass: String,
    val contextId: String,
    val contextName: String?,
)
