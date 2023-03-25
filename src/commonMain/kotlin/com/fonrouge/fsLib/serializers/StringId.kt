package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.objectIdHexString
import kotlinx.serialization.Serializable

@Serializable(with = StringIdSerializer::class)
data class StringId<@Suppress("unused") T>(
    val id: String = objectIdHexString(),
) {
    override fun toString(): String {
        return id
    }
}
