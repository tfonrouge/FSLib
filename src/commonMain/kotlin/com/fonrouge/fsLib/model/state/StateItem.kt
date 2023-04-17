package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class StateItem<T : BaseDoc<*>>(
    val item: T? = null,
    val crudAction: CrudAction = CrudAction.Read,
    val callType: CallType = CallType.Query,
    override val contextClass: String? = null,
    override val contextId: String? = null,
    override val state: String? = null,
) : ContexState() {
    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
