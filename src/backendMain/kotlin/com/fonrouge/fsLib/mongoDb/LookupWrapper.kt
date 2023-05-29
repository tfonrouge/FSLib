package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

interface LookupWrapper<T : BaseDoc<*>, ID : BaseDoc<*>> {
    val lookupWrappers: Array<out LookupWrapper<ID, *>>
}

class LookupByProperty<T : BaseDoc<*>, ID : BaseDoc<*>>(
    val resultProperty: KProperty1<T, ID?>,
    override vararg val lookupWrappers: LookupWrapper<ID, *>
) : LookupWrapper<T, ID>

class LookupByPipeline<T : BaseDoc<*>, ID : BaseDoc<W>, W : Any>(
    val pipeline: LookupPipelineBuilder<T, ID, W>,
    override vararg val lookupWrappers: LookupWrapper<ID, *>
) : LookupWrapper<T, ID>
