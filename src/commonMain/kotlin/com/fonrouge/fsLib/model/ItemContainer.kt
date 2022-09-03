package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemContainer<T>(
    var item: T? = null,
    val itemAlreadyOn: Boolean = false,
    override var isOk: Boolean = item != null,
    override var msgOk: String? = "Operation successful ...",
    override var msgError: String? = "Operation Failed ...",
    override var data: String? = null
) : ISimpleResponse
