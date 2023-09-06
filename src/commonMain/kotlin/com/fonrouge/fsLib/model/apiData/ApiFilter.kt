package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
open class ApiFilter @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    var masterItemIdSerialized: String? = null
) {
    @Suppress("unused")
    inline fun <reified T> masterItemId(): T? {
        return masterItemIdSerialized?.let { Json.decodeFromString<T>(it) }
    }

    @Suppress("unused")
    inline fun <reified T> setMasterIdSerialized(id: T): ApiFilter {
        masterItemIdSerialized = Json.encodeToString(id)
        return this
    }
}
