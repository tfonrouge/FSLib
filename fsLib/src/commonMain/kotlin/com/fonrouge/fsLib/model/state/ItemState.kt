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
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error

    /**
     * Secondary constructor for the `ItemState` class that initializes its properties
     * based on a `SimpleState` instance.
     *
     * This constructor extracts the `state`, `msgOk`, and `msgError` properties from the provided
     * `SimpleState` instance and uses them to initialize an `ItemState` instance.
     *
     * @param simpleResponse An instance of `SimpleState` that provides the values for initializing the `ItemState` instance.
     */
    constructor(simpleResponse: SimpleState) : this(
        state = simpleResponse.state,
        msgOk = simpleResponse.msgOk,
        msgError = simpleResponse.msgError,
    )

    /**
     * Secondary constructor for initializing the `ItemState` with a warning state and corresponding message.
     *
     * This constructor sets the `state` property to `State.Warn` and uses the provided `msgWarn` as the `msgError` value.
     *
     * @param msgWarn The warning message to be associated with this state.
     */
    constructor(msgWarn: String) : this(
        state = State.Warn,
        msgError = msgWarn,
    )

    /**
     * Constructs an `ItemState` instance with a state determined by the provided `isOk` value.
     *
     * @param isOk A boolean indicating whether the state should be `State.Ok` or `State.Error`.
     * @param msgOk An optional message for a successful state. Defaults to `MSG_OK`.
     * @param msgError An optional message for an error state. Defaults to `MSG_ERROR`.
     */
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
    )

    /**
     * A property that provides a simplified representation of the current item state.
     *
     * This property returns an instance of [SimpleState], encapsulating the state,
     * optional success and error messages, and other relevant details from the parent [ItemState].
     */
    val asSimpleState get() = SimpleState(this)
}
