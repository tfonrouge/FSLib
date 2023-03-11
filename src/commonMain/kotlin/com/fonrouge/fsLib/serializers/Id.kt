package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.objectIdHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.reflect.KClass

@OptIn(ExperimentalJsExport::class)
@Serializable(with = IdSerializer::class)
@JsExport
data class Id<T : BaseModel<*>>(
    val id: String = objectIdHexString(),
    @Suppress("NON_EXPORTABLE_TYPE")
    @Transient
    val klass: KClass<T>? = null
) {
    val name: String?
        get() {
            return klass?.simpleName
        }
}
