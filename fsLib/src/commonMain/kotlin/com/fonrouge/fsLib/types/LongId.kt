package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.LongIdSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a generic wrapper for a long identifier.
 *
 * This data class encapsulates a long value to be used as an identifier for an associated type `T`.
 * It provides serialization support through `LongIdSerializer` and overrides the `toString()`
 * method to return the string representation of the long identifier.
 *
 * @param T The type associated with the long identifier. This is currently unused.
 * @property id The long value of the identifier.
 */
@Serializable(with = LongIdSerializer::class)
data class LongId<T : Any>(
    val id: Long,
) {
    override fun toString(): String {
        return "$id"
    }
}