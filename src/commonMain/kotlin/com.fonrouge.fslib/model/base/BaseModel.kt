@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fslib.model.base

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
abstract class BaseModel {
    abstract val id: Any
    abstract val upsertInfo: UpsertInfo?
}
