package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse<T>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    override val isOk: Boolean = item != null,
    val noDataModified: Boolean? = null,
    override val msgOk: String? = "Operation successful ...",
    override val msgError: String? = "Operation Failed ...",
    override val state: String? = null,
) : ISimpleResponse {
    @Suppress("unused")
    constructor(simpleResponse: SimpleResponse) : this(
        isOk = simpleResponse.isOk,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
        state = simpleResponse.state
    )
}
