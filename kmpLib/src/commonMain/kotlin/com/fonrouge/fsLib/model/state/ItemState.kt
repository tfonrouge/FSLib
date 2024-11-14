package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

/**
 * Represents the state of an item, including its data and associated status information.
 *
 * @param T The type of the item.
 * @param item The item data.
 * @param itemAlreadyOn A flag indicating if the item is already active or present.
 * @param noDataModified Optional flag indicating if no data has been modified.
 * @param state Indicates the current state, defaulting to `State.Ok` if item is present, otherwise `State.Error`.
 * @param msgOk A message indicating a successful operation.
 * @param msgError A message indicating a failed operation.
 * @param cargo Optional additional information or payload.
 * @param dateTime A timestamp representing when the state was set.
 * @param hasError A boolean indicating if the current state is an error state.
 */
@Serializable
data class ItemState<T>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    val noDataModified: Boolean? = null,
    override val state: State = if (item != null) State.Ok else State.Error,
    override val msgOk: String? = MSG_OK,
    override val msgError: String? = MSG_ERROR,
    override val cargo: String? = null
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error

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

    val asSimpleState get() = SimpleState(this)
}
