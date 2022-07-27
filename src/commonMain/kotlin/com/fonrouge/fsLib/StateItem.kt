package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.Serializable

@Serializable
class StateItem<T : BaseModel<*>>(
    val item: T? = null,
    val json: String? = null,
    val crudAction: CrudAction,
    val callType: CallType,
    val state: String? = null,
    val contextDataUrl: ContextDataUrl? = null
) {
    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
