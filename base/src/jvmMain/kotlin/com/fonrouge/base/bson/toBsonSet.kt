package com.fonrouge.base.bson

import org.bson.conversions.Bson
import org.litote.kmongo.util.KMongoUtil

/**
 * Converts an object into a BSON representation.
 *
 * @param obj The object to be converted into BSON.
 * @param updateOnlyNotNullProperties If true, only the non-null properties of the object will be included in the BSON output.
 * @return The BSON representation of the object.
 */
@Suppress("unused")
fun <T : Any> toBsonSet(obj: T, updateOnlyNotNullProperties: Boolean = false): Bson {
    return KMongoUtil.toBsonModifier(obj, updateOnlyNotNullProperties = updateOnlyNotNullProperties)
}

/**
 * Converts the current object into a BSON representation.
 *
 * @param updateOnlyNotNullProperties If true, only the non-null properties of the object
 * will be included in the BSON output. Defaults to false.
 * @return The BSON representation of the current object.
 */
@Suppress("unused")
fun <T: Any> T.toBsonSet(updateOnlyNotNullProperties: Boolean = false): Bson = toBsonSet(this, updateOnlyNotNullProperties)
