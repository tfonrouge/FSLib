@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fslib.model.base

import kotlin.js.JsExport

@JsExport
abstract class BaseModel<T> {
    abstract var id: T
//    abstract val upsertInfo: UpsertInfo
}
