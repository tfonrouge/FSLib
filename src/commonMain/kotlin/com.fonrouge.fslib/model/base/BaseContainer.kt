package com.fonrouge.fslib.model.base

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.js.JsExport

//@Serializable
@JsExport
abstract class BaseContainer {
    var version: String = "v1.0"
    var date: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    open fun beforeInsert() {}
}
