package com.fonrouge.fslib.model.base

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
open class BaseContainer {
    var version: String? = null
    var date: String? = null

    open fun beforeInsert() {}
}
