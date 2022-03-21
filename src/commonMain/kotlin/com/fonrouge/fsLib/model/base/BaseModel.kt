@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fsLib.model.base

import kotlin.js.JsExport

//@Serializable
@JsExport
abstract class BaseModel<T> {
    abstract var id: T
//    abstract val upsertInfo: UpsertInfo?
}
