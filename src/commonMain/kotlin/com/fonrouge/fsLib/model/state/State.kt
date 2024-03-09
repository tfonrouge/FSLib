package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
enum class State {
    Ok,
    Warn,
    Error,
}
