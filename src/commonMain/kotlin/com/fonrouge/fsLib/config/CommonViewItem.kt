package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataItem
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.reflect.KClass

abstract class CommonViewItem<T : BaseDoc<ID>, ID : Any, E : IDataItem, FILT : IApiFilter>(
    private val function: suspend E.(ApiItem<T, ID, FILT>) -> ItemState<T>,
    val labelIdFunc: ((T?) -> String?)? = { it?._id?.toString() ?: "<no-item>" },
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
