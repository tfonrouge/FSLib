package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ApiFilter {
    var masterItemIdSerialized: String?
}

@Suppress("unused")
inline fun <reified T> ApiFilter.masterItemId(): T? {
    return masterItemIdSerialized?.let { Json.decodeFromString<T>(it) }
}

@Suppress("unused")
inline fun <reified T> ApiFilter.serializeMasterItemId(id: T): ApiFilter {
    masterItemIdSerialized = Json.encodeToString(id)
    return this
}
