package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ItemState<T>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    val noDataModified: Boolean? = null,
    override val isOk: Boolean = item != null,
    override val state: ISimpleState.State = if (isOk) ISimpleState.State.Ok else ISimpleState.State.Warn,
    override val msgOk: String? = "Operation successful ...",
    override val msgError: String? = "Operation Failed ...",
    override val cargo: String? = null
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()

    @Suppress("unused")
    constructor(simpleResponse: SimpleState) : this(
        isOk = simpleResponse.isOk,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
        state = simpleResponse.state,
    )

    @Suppress("unused")
    val asSimpleState get() = SimpleState(this)
}
