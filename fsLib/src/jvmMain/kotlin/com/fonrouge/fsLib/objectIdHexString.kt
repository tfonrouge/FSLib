package com.fonrouge.fsLib

import org.bson.types.ObjectId

/**
 * Generates a new ObjectId in its hexadecimal string representation.
 *
 * This function creates a unique identifier using the ObjectId format,
 * commonly utilized in MongoDB, and returns its hexadecimal string equivalent.
 *
 * @return A hexadecimal string representation of a newly generated ObjectId.
 */
actual fun objectIdHexString(): String = ObjectId().toHexString()
