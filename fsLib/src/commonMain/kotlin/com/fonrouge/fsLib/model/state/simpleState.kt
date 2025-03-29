package com.fonrouge.fsLib.model.state

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
