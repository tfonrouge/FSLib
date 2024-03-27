package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable

@Serializable
enum class CallType {
    Query,
    Action
}
