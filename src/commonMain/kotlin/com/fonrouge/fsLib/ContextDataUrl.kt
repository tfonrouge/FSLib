package com.fonrouge.fsLib

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ContextDataUrl(
    var filter: String? = null,
    var sorter: String? = null,
    var contextClass: String? = null,
    var contextId: String? = null,
    var params: String? = null,
    var json: String? = null
) {
    @Suppress("unused")
    inline fun <reified T> contextIdValue(): T? {
        return contextId?.let { Json.decodeFromString(it) }
    }
}

@Suppress("unused")
val String?.contextDataUrl: ContextDataUrl?
    get() {
        return this?.let { Json.decodeFromString<ContextDataUrl>(it) }
    }
