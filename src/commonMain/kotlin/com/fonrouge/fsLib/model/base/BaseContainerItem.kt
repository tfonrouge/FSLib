package com.fonrouge.fsLib.model.base

import kotlin.js.JsExport

@JsExport
abstract class BaseContainerItem<T : BaseModel<*>> : BaseContainer() {
    abstract var item: T?
}
