package com.fonrouge.base.state

import com.fonrouge.base.offsetDateTimeNow
import com.fonrouge.base.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

/**
 * Represents the state of an item, including its current status, associated messages,
 * and additional metadata such as timestamps and internal structure.
 *
 * @param T The type of the item this state pertains to.
 * @property item The item associated with this state. May be null if no item is applicable.
 * @property itemAlreadyOn Indicates whether the item is already active or present.
 * @property noDataModified Specifies whether any data has been modified. May be null if not applicable.
 * @property valueMap A map containing key-value pairs of data associated with this state. Keys are property name strings,
 * while values may be nullable strings with serialized values.
 * @property state The current state of this item, represented using the [State] enum. Defaults to `State.Ok`
 * if an item exists or `valueMap` is not empty. Otherwise, it defaults to `State.Error`.
 * @property msgOk An optional message indicating a successful state. Defaults to `MSG_OK`.
 * @property msgError An optional message describing an error state. Defaults to `MSG_ERROR` if the state
 * is not `State.Ok`, otherwise null.
 * @property dateTime The timestamp when this state was defined or last modified.
 * @property hasError A boolean indicating whether the current state represents an error.
 */
@Serializable
data class ItemState<T>(
    val item: T? = null,
    val itemAlreadyOn: Boolean = false,
    val noDataModified: Boolean? = null,
    val valueMap: Map<String, String?>? = null,
    override val state: State = if (item != null || valueMap.isNullOrEmpty().not()) State.Ok else State.Error,
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
