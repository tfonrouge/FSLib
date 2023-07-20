package com.fonrouge.fsLib.serializers

import kotlinx.serialization.Serializable

@Serializable(with = StringIdSerializer::class)
data class StringId<@Suppress("unused") T>(
    val id: String = "",
) {
    override fun toString(): String {
        return id
    }
}
