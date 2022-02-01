package com.fonrouge.fslib.model.base

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
abstract class BaseContainerList<T : BaseModel> : BaseContainer() {
    abstract var list: List<T>?
    var listCRC32: String? = null
    var listSize: Int = 0
}
