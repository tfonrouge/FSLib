package com.fonrouge.fsLib.model.state

import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

interface ISimpleState {
    val isOk: Boolean
    val msgOk: String?
    val msgError: String?
    val state: State
    val cargo: String?
    val dateTime: OffsetDateTime

    @Serializable
    enum class State {
        Ok,
        Warn,
        Error,
    }
}
