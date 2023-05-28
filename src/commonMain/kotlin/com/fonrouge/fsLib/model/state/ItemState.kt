package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
data class ItemState<T, STATE>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    val noDataModified: Boolean? = null,
    val apiState: STATE? = null,
    override val isOk: Boolean = item != null,
    override val msgOk: String? = "Operation successful ...",
    override val msgError: String? = "Operation Failed ...",
) : ISimpleState {
    @Suppress("unused")
    constructor(simpleResponse: SimpleState) : this(
        isOk = simpleResponse.isOk,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
    )
}
