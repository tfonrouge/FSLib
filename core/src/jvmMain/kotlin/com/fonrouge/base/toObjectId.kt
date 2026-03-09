package com.fonrouge.base

import com.fonrouge.base.types.OId
import org.bson.types.ObjectId

/**
 * Converts an optional `OId<Any>` instance to an `ObjectId`, if present.
 *
 * If the receiver `OId<Any>` is null, the function returns null. If the receiver
 * is not null, it converts the encapsulated string ID into an `ObjectId` and returns it.
 *
 * @return An `ObjectId` representing the encapsulated identifier, or null if the receiver is null.
 */
@Suppress("unused")
fun OId<Any>?.toObjectId(): ObjectId? {
    return this?.id?.let { ObjectId(it) }
}
