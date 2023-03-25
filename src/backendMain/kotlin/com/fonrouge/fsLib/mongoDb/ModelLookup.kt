package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

class ModelLookup<T : BaseDoc<*>, U : BaseDoc<*>>(
    val resultProperty: KProperty1<T, U?>,
    vararg val modelLookups: ModelLookup<U, *>
)
