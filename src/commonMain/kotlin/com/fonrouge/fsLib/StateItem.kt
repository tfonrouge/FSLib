package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("unused")
@Serializable
class StateItem<T : BaseModel<*>>(
    var item: T? = null,
    var json: String? = null,
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
