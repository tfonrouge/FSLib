package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SimpleState(
    override val state: State,
    override val msgOk: String? = null,
    override val msgError: String? = null,
    override val cargo: String? = null
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean
        get() = state == State.Error

    @Suppress("unused")
    constructor(itemState: ItemState<*>) : this(
        state = itemState.state,
        msgOk = itemState.msgOk,
        msgError = itemState.msgError
    )

    constructor(
        isOk: Boolean,
        msgOk: String? = MSG_OK,
        msgError: String? = MSG_ERROR
    ) : this(
        state = if (isOk) {
            State.Ok
        } else {
            State.Error
        },
        msgOk = msgOk,
        msgError = msgError
    )
}
