package com.fonrouge.fsLib.state

import kotlinx.serialization.Serializable

/**
 * An enumeration that represents the state of an item or process.
 *
 * The `State` enum provides a standardized way to express the status
 * of an entity. It includes the following states:
 *
 * - `Ok`: Indicates that the item or process is in a healthy or successful state.
 * - `Warn`: Indicates that the item or process is in a state that requires attention but is not critical.
 * - `Error`: Indicates that the item or process is in a failed or problematic state.
 */
@Serializable
enum class State {
    Ok,
    Warn,
    Error,
}
