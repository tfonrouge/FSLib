package com.fonrouge.fslib.model.base

import kotlin.js.JsExport

//@Serializable
@JsExport
abstract class BaseContainerItem<T : BaseModel<*>> : BaseContainer() {
    abstract var item: T?
}
