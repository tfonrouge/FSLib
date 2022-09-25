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
) : ISimpleResponse {
    @Suppress("unused")
    constructor(simpleResponse: SimpleResponse) : this(
        isOk = simpleResponse.isOk,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
        data = simpleResponse.data
    )
}
