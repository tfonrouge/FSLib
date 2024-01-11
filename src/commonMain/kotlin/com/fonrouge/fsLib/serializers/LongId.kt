package com.fonrouge.fsLib.serializers

import kotlinx.serialization.Serializable

@Serializable(with = LongIdSerializer::class)
data class LongId<@Suppress("unused") T : Any>(
    val id: Long,
) {
    override fun toString(): String {
        return "$id"
    }
}
