package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val id: ID? = null,
    val item: T? = null,
    val callType: CallType = CallType.Query,
    val crudTask: CrudTask = CrudTask.Read,
    val apiFilter: FILT? = null,
) {
    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
