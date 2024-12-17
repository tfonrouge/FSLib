package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.IntIdSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a wrapper for an integer identifier that can be used generically with any type.
 *
 * This class is designed to associate a strongly typed integer identifier with an underlying
 * type `T`. It provides serialization capabilities via `IntIdSerializer` and includes
 * functionality to return the identifier as a string representation.
 *
 * @param T The type associated with the integer identifier. This is currently unused.
 * @property id The integer value of the identifier.
 */
@Serializable(with = IntIdSerializer::class)
data class IntId<@Suppress("unused") T : Any>(
    val id: Int,
) {
    override fun toString(): String {
        return "$id"
    }
}