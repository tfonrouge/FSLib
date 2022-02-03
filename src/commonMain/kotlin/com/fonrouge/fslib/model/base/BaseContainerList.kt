package com.fonrouge.fslib.model.base

import kotlin.js.JsExport

//@Serializable
@JsExport
abstract class BaseContainerList<T : BaseModel<*>> : BaseContainer() {
    abstract var list: Array<T>
    var listCRC32: String = ""
}
