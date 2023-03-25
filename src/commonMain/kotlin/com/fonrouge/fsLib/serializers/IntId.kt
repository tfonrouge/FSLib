package com.fonrouge.fsLib.serializers

import kotlinx.serialization.Serializable

@Serializable(with = IntIdSerializer::class)
data class IntId<@Suppress("unused") T>(
    val id: Int,
) {
    override fun toString(): String {
        return "$id"
    }
}
