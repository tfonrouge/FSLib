package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.IntIdSerializer
import kotlinx.serialization.Serializable

@Serializable(with = IntIdSerializer::class)
data class IntId<@Suppress("unused") T : Any>(
    val id: Int,
) {
    override fun toString(): String {
        return "$id"
    }
}