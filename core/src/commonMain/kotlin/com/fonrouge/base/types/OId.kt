package com.fonrouge.base.types

import com.fonrouge.base.objectIdHexString
import com.fonrouge.base.serializers.OIdSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a generic wrapper for an identifier using a standardized object ID format.
 *
 * This class is designed to encapsulate a string-based identifier, commonly generated in
 * the form of a MongoDB-like ObjectId. It provides serialization support via the
 * `OIdSerializer` and can be used generically to associate an object ID with a specified type `T`.
 *
 * @param T The type associated with the identifier. This is currently unused.
 * @property id The string value of the ObjectId. Defaults to a newly generated ObjectId string.
 */
@Serializable(with = OIdSerializer::class)
data class OId<T>(
    override val id: String = objectIdHexString(),
) : IBaseId<String>
