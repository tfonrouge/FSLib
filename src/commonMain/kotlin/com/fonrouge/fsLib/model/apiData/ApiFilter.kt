package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@OptIn(ExperimentalSerializationApi::class)
open class ApiFilter {
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    open var masterItemIdSerialized: String? = null
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
