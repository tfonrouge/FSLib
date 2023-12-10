package com.fonrouge.fsLib.model.state

import io.kvision.types.OffsetDateTime

interface ISimpleState {
    val isOk: Boolean
    val msgOk: String?
    val msgError: String?
    val state: String?
    val dateTime: OffsetDateTime
}
