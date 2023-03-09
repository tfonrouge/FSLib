package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.newObjectId
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.reflect.KClass

@OptIn(ExperimentalJsExport::class)
@Serializable(with = FSIdSerializer::class)
@JsExport
class FSId<T : BaseModel<*>>(
    val id: String = newObjectId(),
    @Suppress("NON_EXPORTABLE_TYPE") private val klass: KClass<T>? = null
) {
    val name: String? get() {
        return klass?.simpleName
    }
}
