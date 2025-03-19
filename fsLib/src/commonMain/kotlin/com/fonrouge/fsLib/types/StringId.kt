package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.serializers.StringIdSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a strongly typed wrapper for a string identifier.
 *
 * This class is designed for cases where a string-based identifier needs to
 * be associated with a specific type `T`. It provides a straightforward way
 * to work with string identifiers, ensuring clarity and type safety.
 *
 * The `toString` method is overridden to return the string representation of
 * the identifier for convenience.
 *
 * @param T The type associated with the string identifier. This is currently unused.
 * @property id The string value of the identifier.
 */
@Serializable(with = StringIdSerializer::class)
data class StringId<T>(
    override val id: String,
) : IBaseId<String> {
    override fun toString(): String {
        return id
    }
}
