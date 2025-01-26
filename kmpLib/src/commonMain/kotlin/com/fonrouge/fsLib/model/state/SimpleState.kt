package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

/**
 * A data class that represents a simple state with associated messages and additional information.
 *
 * This class provides a lightweight representation of state information, including:
 * - The current state of type [State].
 * - Optional success and error messages.
 * - Optional cargo or payload.
 * - A timestamp indicating when the state was created.
 * - A boolean property indicating whether the state represents an error.
 *
 * The class includes utility constructors to facilitate initialization from various inputs
 * and conversion methods to interoperate with related types like [ItemState].
 *
 * @property state The current state of this instance.
 * @property msgOk An optional message indicating success.
 * @property msgError An optional message indicating an error.
 * @property cargo Optional additional information or payload.
 * @property dateTime A timestamp representing when this instance was created.
 * @property hasError A boolean indicating if the state represents an error condition.
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

    @Suppress("unused")
    constructor(msgWarn: String) : this(
        state = State.Warn,
        msgError = msgWarn,
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
