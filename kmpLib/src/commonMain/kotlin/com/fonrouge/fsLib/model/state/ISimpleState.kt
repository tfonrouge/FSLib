package com.fonrouge.fsLib.model.state

import io.kvision.types.OffsetDateTime

const val MSG_OK = "Operation successful"
const val MSG_ERROR = "Operation Failed"

interface ISimpleState {
    val state: State
    val msgOk: String?
    val msgError: String?
    val cargo: String?
    val dateTime: OffsetDateTime

    val isOk: Boolean
}
