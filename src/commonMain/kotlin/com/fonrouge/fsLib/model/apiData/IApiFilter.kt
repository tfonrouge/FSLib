package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface IApiFilter {
    var masterItemIdSerialized: String?
}

@Suppress("unused")
inline fun <reified T> IApiFilter.masterItemId(): T? {
    return masterItemIdSerialized?.let { Json.decodeFromString<T>(it) }
}

@Suppress("unused")
inline fun <reified T, FILT : IApiFilter> FILT.serializeMasterItemId(id: T): FILT {
    masterItemIdSerialized = Json.encodeToString(id)
    return this
}
