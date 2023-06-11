package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
abstract class ContexApi : IContextApi {
    inline fun <reified V> contextIdValue(): V? {
        return contextId?.let { Json.decodeFromString(it) }
    }

    inline fun <reified T> stateValue(): T? {
        return state?.let { Json.decodeFromString(it) }
    }
}
