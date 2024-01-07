package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<V : ViewDataContainer<FILT>, FILT : IApiFilter>(
    apiFilterSerializer: KSerializer<FILT>,
    viewFunc: KClass<out V>,
    baseUrl: String,
    requireCredentials: Boolean,
    override val commonView: ICommonViewContainer<FILT>
) : ConfigView<V, FILT>(
    viewFunc = viewFunc,
    apiFilterSerializer = apiFilterSerializer,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
)
