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
 * @property dateTime A timestamp representing when this instance was created.
 * @property hasError A boolean indicating if the state represents an error condition.
 */
@Serializable
data class SimpleState(
    override val state: State,
    override val msgOk: String? = null,
    override val msgError: String? = null,
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error

    /**
     * Secondary constructor for the `SimpleState` class that initializes its properties
     * based on an `ItemState` instance.
     *
     * This constructor extracts the `state`, `msgOk`, and `msgError` properties from the provided
     * `ItemState` instance and uses them to initialize a `SimpleState` instance.
     *
     * @param itemState An instance of `ItemState` that provides the values for initializing the `SimpleState` instance.
     */
    constructor(itemState: ItemState<*>) : this(
        state = itemState.state,
        msgOk = itemState.msgOk,
        msgError = itemState.msgError
    )

    /**
     * Secondary constructor for initializing a `SimpleState` instance with a warning state.
     *
     * This constructor sets the `state` property to `State.Warn` and assigns the provided `msgWarn`
     * value to the `msgError` property. It is used to represent a non-critical condition that requires
     * attention or action.
     *
     * @param msgWarn A string containing the warning message to be associated with the state.
     */
    @Suppress("unused")
    constructor(msgWarn: String) : this(
        state = State.Warn,
        msgError = msgWarn,
    )

    /**
     * Secondary constructor for initializing a `SimpleState` instance based on a boolean condition.
     *
     * This constructor determines the `state` property of the `SimpleState` instance based on the value
     * of the `isOk` parameter:
     * - If `isOk` is `true`, the `state` is set to `State.Ok`.
     * - Otherwise, the `state` is set to `State.Error`.
     *
     * Additionally, optional success and error messages can be provided through the `msgOk` and `msgError`
     * parameters, defaulting to predefined constants `MSG_OK` and `MSG_ERROR`, respectively.
     *
     * @param isOk A boolean indicating whether the state should be `State.Ok` or `State.Error`.
     * @param msgOk An optional success message. Defaults to `MSG_OK`.
     * @param msgError An optional error message. Defaults to `MSG_ERROR`.
     */
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

    /**
     * Transforms the current `SimpleState` instance into an `ItemState` instance.
     *
     * This method provides a convenient way to convert a `SimpleState` object
     * into an `ItemState` object. The properties from the `SimpleState` instance,
     * such as `state`, `msgOk`, and `msgError`, are used to initialize the
     * corresponding properties in the resulting `ItemState` object.
     *
     * @param T The type parameter corresponding to the type of the item in `ItemState`.
     * @return A new instance of `ItemState` initialized from the current `SimpleState`.
     */
    fun <T> asItemState() = ItemState<T>(this)
}

/**
 * Creates a `SimpleState` instance representing an error state.
 *
 * This method is a utility function to quickly generate a `SimpleState` object
 * with the state set to `State.Error` and an associated error message.
 *
 * @param msgError An optional error message for the error state. Defaults to the constant `MSG_ERROR`.
 */
@Suppress("unused")
fun simpleErrorState(msgError: String = MSG_ERROR) = SimpleState(State.Error, msgError)

/**
 * Creates an instance of `SimpleState` with a `Warn` state and a specified warning message.
 *
 * This method provides a convenient way to create a warning state with a custom message.
 * If no message is provided, a default warning message is used.
 *
 * @param msgWarn The warning message to associate with the state. Defaults to `MSG_ERROR`.
 * @return A `SimpleState` instance with a `Warn` state and the specified warning message.
 */
@Suppress("unused")
fun simpleWarnState(msgWarn: String = MSG_ERROR) = SimpleState(State.Warn, msgWarn)
