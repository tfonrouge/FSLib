package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

open class LookupWrapper<T : BaseDoc<*>, U : BaseDoc<*>>(
    open val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
)

class LookupByProperty<T : BaseDoc<*>, U : BaseDoc<*>>(
    val resultProperty: KProperty1<T, U?>,
    override val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
) : LookupWrapper<T, U>()

class LookupByPipeline<T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any>(
    val pipeline: LookupPipelineBuilder<T, U, ID>,
    override val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
) : LookupWrapper<T, U>()
