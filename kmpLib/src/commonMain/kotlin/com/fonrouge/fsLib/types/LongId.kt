package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.LongIdSerializer
import kotlinx.serialization.Serializable

@Serializable(with = LongIdSerializer::class)
data class LongId<@Suppress("unused") T : Any>(
    val id: Long,
) {
    override fun toString(): String {
        return "$id"
    }
}