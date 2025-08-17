package com.fonrouge.fsLib.state

import io.kvision.types.OffsetDateTime

const val MSG_OK = "Operation successful"
const val MSG_ERROR = "Operation Failed"

/**
 * Represents a simple state interface that encapsulates the current state, associated messages,
 * metadata, and details about the state occurrence.
 *
 * The interface provides a standardized way to express and measure the conditions or status
 * of an object, process, or item through the following properties:
 *
 * @property state The current state of the object or process, represented using the `State` enum.
 * @property msgOk An optional message indicating successful status, if applicable.
 * @property msgError An optional message describing an error or problematic condition, if applicable.
 * @property dateTime The timestamp when the state was defined or last modified.
 * @property hasError A boolean value that indicates whether the state represents an error condition.
 */
interface ISimpleState {
    val state: State
    val msgOk: String?
    val msgError: String?
    val dateTime: OffsetDateTime
    val hasError: Boolean
}
