package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

interface LookupWrapper<T : BaseDoc<*>, U : BaseDoc<*>> {
    val lookupWrappers: Array<out LookupWrapper<U, *>>
}

class LookupByProperty<T : BaseDoc<*>, U : BaseDoc<*>>(
    val resultProperty: KProperty1<T, U?>,
    override vararg val lookupWrappers: LookupWrapper<U, *>
) : LookupWrapper<T, U>

class LookupByPipeline<T : BaseDoc<*>, U : BaseDoc<W>, W : Any>(
    val pipeline: LookupPipelineBuilder<T, U, W>,
    override vararg val lookupWrappers: LookupWrapper<U, *>
) : LookupWrapper<T, U>
