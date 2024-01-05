package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseDoc<ID>, V : ViewDataContainer<FILT>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>,
    name: String,
    viewFunc: KClass<out V>,
    baseUrl: String,
    requireCredentials: Boolean,
//    commonView: CommonViewContainer<T, ID, FILT>
    commonView: CommonView<FILT>
) : ConfigView<V, FILT>(
    name = name,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
)
