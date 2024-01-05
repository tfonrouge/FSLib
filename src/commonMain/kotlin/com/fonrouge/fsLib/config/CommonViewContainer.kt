package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KClass

abstract class CommonViewContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>,
    label: String,
    apiFilterKClass: KClass<FILT>,
) : CommonView<FILT>(label = label, apiFilterKClass = apiFilterKClass)
