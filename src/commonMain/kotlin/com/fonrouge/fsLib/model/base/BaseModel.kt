package com.fonrouge.fsLib.model.base

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface BaseModel<T> {
    val _id: T
}
