package com.fonrouge.fsLib.model.base

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

//@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
interface BaseModel<T> {
    var id: T
//    abstract val upsertInfo: UpsertInfo?
}
