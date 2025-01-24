package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

/**
 * Represents the state of an item with additional information and status metadata.
 *
 * This class encapsulates state information for a particular item, providing details such as:
 * - The item itself, which may be null.
 * - A flag to indicate whether the item is already present.
 * - A flag to indicate whether any data has been modified.
 * - The current state of the item, which defaults to `State.Ok` if the item is not null,
 *   or `State.Error` otherwise.
 * - Optional messages for success or error scenarios.
 * - Optional metadata or cargo associated with the item's state.
 * - A timestamp indicating when the state was created or last updated.
 * - A boolean property indicating whether the state represents an error.
 *
 * This class also provides several constructors for convenient initialization:
 * - From an existing simple state.
 * - With a warning message.
 * - With a boolean indicating success or failure, along with optional messages and cargo.
 *
 * Utility methods are available for converting to a `SimpleState` representation.
 *
 * @param T The type of the item this state represents.
 * @property item The item associated with this state, or null if not present.
 * @property itemAlreadyOn A flag indicating if the item is already active or present.
 * @property noDataModified An optional flag indicating if no data has been modified.
 * @property state The current state of the item, conforming to the `State` enum.
 * @property msgOk An optional success message.
 * @property msgError An optional error message.
 * @property cargo Additional metadata or payload associated with the item state.
 * @property dateTime A timestamp representing when this state was created or modified.
 * @property hasError A boolean indicating if the state represents an error condition.
 */
@Serializable
data class ItemState<T>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    val noDataModified: Boolean? = null,
    override val state: State = if (item != null) State.Ok else State.Error,
    override val msgOk: String? = MSG_OK,
    override val msgError: String? = if (state != State.Ok) MSG_ERROR else null,
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

    @Suppress("unused")
    constructor(msgWarn: String) : this(
        state = State.Warn,
        msgError = msgWarn,
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
