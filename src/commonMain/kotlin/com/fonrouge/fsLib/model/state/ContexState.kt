package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
abstract class ContexState : IContextState {
    inline fun <reified V> contextIdValue(): V {
        return Json.decodeFromString(contextId ?: "")
    }

    inline fun <reified T> stateValue(): T? {
        return state?.let { Json.decodeFromString(it) }
    }
}
