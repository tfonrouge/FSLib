package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.objectIdHexString
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@Serializable(with = IdSerializer::class)
@JsExport
data class Id<@Suppress("unused") T>(
    val id: String = objectIdHexString(),
)
