package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import kotlin.reflect.KProperty1

class ModelLookup<T : BaseModel<*>, U : BaseModel<*>>(
    val resultProperty: KProperty1<T, U?>,
    vararg val modelLookup: ModelLookup<U, *>
)
