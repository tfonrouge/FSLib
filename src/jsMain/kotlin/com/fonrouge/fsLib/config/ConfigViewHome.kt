package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.view.KVWebManager.configViewHome
import com.fonrouge.fsLib.view.ViewHome
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class ConfigViewHome<V : ViewHome<FILT>, FILT : IApiFilter>(
    name: String,
    label: String,
    viewFunc: KClass<V>,
    apiFilterKClass: KClass<FILT>,
    baseUrl: String = "",
    requireCredentials: Boolean = true,
) : ConfigView<V, FILT>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials
) {
    init {
        configViewHome = this
    }
}
