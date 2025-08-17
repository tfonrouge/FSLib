package com.fonrouge.base.api

import kotlinx.serialization.Serializable

/**
 * Defines the types of API calls that can be made.
 */
@Serializable
enum class CallType {
    Query,
    Action
}
