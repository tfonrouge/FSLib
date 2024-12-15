package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.StringIdSerializer
import kotlinx.serialization.Serializable

@Serializable(with = StringIdSerializer::class)
data class StringId<@Suppress("unused") T>(
    val id: String,
) {
    override fun toString(): String {
        return id
    }
}