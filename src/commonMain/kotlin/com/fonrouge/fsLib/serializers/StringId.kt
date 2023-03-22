package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.objectIdHexString
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@Serializable(with = StringIdSerializer::class)
@JsExport
data class StringId<@Suppress("unused") T>(
    val id: String = objectIdHexString(),
) {
    override fun toString(): String {
        return id
    }
}
