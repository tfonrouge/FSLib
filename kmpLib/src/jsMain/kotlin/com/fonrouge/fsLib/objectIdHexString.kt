package com.fonrouge.fsLib

/**
 * Generates a default hexadecimal string representation of an ObjectId.
 *
 * This function returns a 24-character string consisting of zeros,
 * commonly used as a default or placeholder ObjectId value.
 *
 * @return A `String` containing 24 zero characters, representing a default ObjectId value.
 */
actual fun objectIdHexString(): String {
    return "0".repeat(24)
}
