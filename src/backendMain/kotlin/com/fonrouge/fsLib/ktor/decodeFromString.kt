package com.fonrouge.fsLib.ktor

import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.serialization.json.Json

/**
 * Helper to deserialize query parameters
 *
 * @param s string to deserialize
 * @return type [T]
 */
@Suppress("unused")
inline fun <reified T> Parameters.decodeFromString(s: String): T? {
    return Json.decodeFromString(getOrFail(s))
}
