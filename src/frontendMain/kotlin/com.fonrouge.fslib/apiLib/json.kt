package com.fonrouge.fslib.apiLib

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Suppress("unused")
fun csvToJsonObject(stringList: String) = buildJsonObject {
    stringList.split(',').forEach {
        put(it.trim(), true)
    }
}

private val json = Json {
    isLenient = true
}

@Suppress("unused")
fun String.toJsonObject(): JsonObject? {
    try {
        val j = json.parseToJsonElement(this)
        if (j is JsonObject) {
            return j
        }
    } catch (e: Exception) {
        console.error("error on String.toJsonObject()", e)
    }
    return null
}
