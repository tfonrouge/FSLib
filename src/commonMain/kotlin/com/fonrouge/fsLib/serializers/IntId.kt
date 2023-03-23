package com.fonrouge.fsLib.serializers

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@Serializable(with = IntIdSerializer::class)
@JsExport
data class IntId<@Suppress("unused") T>(
    val id: Int,
) {
    override fun toString(): String {
        return "$id"
    }
}
