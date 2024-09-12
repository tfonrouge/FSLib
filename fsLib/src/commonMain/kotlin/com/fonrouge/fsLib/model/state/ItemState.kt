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
    override val state: State = if (item != null) State.Ok else State.Warn,
    override val msgOk: String? = MSG_OK,
    override val msgError: String? = MSG_ERROR,
    override val cargo: String? = null
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val isOk: Boolean
        get() = state == State.Ok

    @Suppress("unused")
    constructor(simpleResponse: SimpleState) : this(
        state = simpleResponse.state,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
    )

    constructor(
        isOk: Boolean,
        msgOk: String? = MSG_OK,
        msgError: String? = MSG_ERROR,
        cargo: String? = null,
    ) : this(
        state = if (isOk) {
            State.Ok
        } else {
            State.Error
        },
        msgOk = msgOk,
        msgError = msgError,
        cargo = cargo
    )

    @Suppress("unused")
    val asSimpleState get() = SimpleState(this)
}
