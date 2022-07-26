package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainerCallType
import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class StateItem<T : BaseModel<*>>(
    val item: T?,
    val json: JsonObject?,
    val crudAction: CrudAction,
    val itemContainerCallType: ItemContainerCallType,
    val state: String?,
)
