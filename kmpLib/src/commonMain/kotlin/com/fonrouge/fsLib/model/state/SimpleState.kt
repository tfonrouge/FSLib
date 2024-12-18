package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

/**
 * Represents a simple state model that provides detailed status information.
 *
 * @property state The current state of the object, which can be `State.Ok`, `State.Warn`, or `State.Error`.
 * @property msgOk An optional message indicating a successful operation.
 * @property msgError An optional message indicating a failed operation.
 * @property cargo Optional additional information or payload associated with the state.
 * @property dateTime Timestamp indicating when the state was set.
 * @property hasError A boolean indicating if the current state is an error state.
 *
 * @constructor Initializes a new instance of `SimpleState` with the provided parameters.
 *
 * @constructor Creates a `SimpleState` based on the given `ItemState`.
 * @param itemState The `ItemState` to derive the new `SimpleState` from.
 *
 * @constructor Creates a `SimpleState` with the specified success or error state.
 * @param isOk Boolean flag to set the state as `Ok` if true, otherwise `Error`.
 * @param msgOk An optional message indicating a successful operation.
 * @param msgError An optional message indicating a failed operation.
 */
@Serializable
data class SimpleState(
    override val state: State,
    override val msgOk: String? = null,
    override val msgError: String? = null,
    override val cargo: String? = null
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error

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

    fun <T> asItemState() = ItemState<T>(this)
}
