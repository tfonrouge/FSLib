package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
open class ApiFilter(
    var masterItemIdSerialized: String? = null
) {
    @Suppress("unused")
    inline fun <reified T> masterItemId(): T? {
        return masterItemIdSerialized?.let { Json.decodeFromString<T>(it) }
    }

    @Suppress("unused")
    inline fun <reified T> serializeMasterItemId(id: T): ApiFilter {
        masterItemIdSerialized = Json.encodeToString(id)
        return this
    }
}
