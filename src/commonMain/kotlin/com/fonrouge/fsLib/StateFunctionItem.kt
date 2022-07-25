package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainerCallType
import kotlinx.serialization.Serializable

@Serializable
class StateFunctionItem(
    val crudAction: CrudAction,
    val itemContainerCallType: ItemContainerCallType,
    val state: String?
)
