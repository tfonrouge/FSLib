package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class StateItem<T : BaseModel<*>>(
    val item: T? = null,
    val json: JsonObject? = null,
    val crudAction: CrudAction,
    val callType: CallType,
    val state: String? = null,
) {
    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
