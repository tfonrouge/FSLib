package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<V : ViewDataContainer<FILT>, FILT : IApiFilter>(
    apiFilterKClass: KClass<FILT>,
    viewFunc: KClass<out V>,
    baseUrl: String,
    requireCredentials: Boolean,
    override val commonView: ICommonViewContainer<FILT>
) : ConfigView<V, FILT>(
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
)
