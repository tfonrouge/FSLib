package com.fonrouge.fsLib.model.base

import io.kvision.types.LocalDateTime
import kotlin.js.JsExport

@JsExport
abstract class BaseContainer {
    abstract var version: String?
    abstract var date: LocalDateTime? //= Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    open fun beforeInsert() {}
}
