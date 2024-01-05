package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseDoc<ID>, V : ViewDataContainer<FILT>, ID : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>,
    apiFilterKClass: KClass<FILT>,
    name: String,
    viewFunc: KClass<out V>,
    baseUrl: String,
    requireCredentials: Boolean,
    override val commonView: ICommonViewContainer<FILT>
) : ConfigView<V, FILT>(
    name = name,
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
)
