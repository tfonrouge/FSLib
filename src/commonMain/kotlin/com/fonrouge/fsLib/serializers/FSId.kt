package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.newObjectId
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable(with = FSIdSerializer::class)
class FSId<T : BaseModel<*>>(
    val id: String = newObjectId(),
    val klass: KClass<T>? = null
)
