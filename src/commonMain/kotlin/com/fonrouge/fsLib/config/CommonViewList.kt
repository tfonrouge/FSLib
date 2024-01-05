package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import kotlin.reflect.KClass

open class CommonViewList<T : BaseDoc<ID>, ID : Any, E : IDataList, FILT : IApiFilter>(
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
    itemKClass: KClass<T>,
    idKClass: KClass<ID>,
    label: String,
    apiFilterKClass: KClass<FILT>,
) : CommonViewContainer<T, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    label = label,
    apiFilterKClass = apiFilterKClass
)
