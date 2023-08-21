package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil

/**
 * Creates an update that sets te value of the provided value obj
 *
 * @param obj the update object
 * @param updateOnlyNotNullProperties
 */
@Suppress("unused")
fun <T : Any> toSet(obj: T, updateOnlyNotNullProperties: Boolean = false): Bson {
    return KMongoUtil.toBsonModifier(obj, updateOnlyNotNullProperties = updateOnlyNotNullProperties)
}
