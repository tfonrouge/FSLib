package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("unused")
@Serializable
data class StateItem<T : BaseDoc<*>>(
    val item: T? = null,
    val crudAction: CrudAction = CrudAction.Read,
    val callType: CallType = CallType.Query,
    val state: String? = null,
    val contextDataUrl: ContextDataUrl? = null
) {
    inline fun <reified V> contextId(): V {
        return Json.decodeFromString(contextDataUrl?.contextId ?: "")
    }

    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
