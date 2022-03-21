package com.fonrouge.fsLib.model.base

import kotlin.js.JsExport

//@Serializable
@JsExport
abstract class BaseContainerList<T : BaseModel<*>> : BaseContainer() {
    abstract var list: Array<T>
    abstract var listCRC32: String?
}
