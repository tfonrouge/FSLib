package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class StateItem<T : BaseDoc<*>>(
    val item: T? = null,
    val callType: CallType = CallType.Query,
    val crudTask: CrudTask = CrudTask.Read,
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
