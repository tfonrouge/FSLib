package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseDoc<ID>, V : ViewDataContainer<FILT>, ID : Any, FILT : ApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>,
    apiFilterKClass: KClass<FILT>,
    name: String,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String,
    requireCredentials: Boolean,
) : ConfigView<V, FILT>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
)
