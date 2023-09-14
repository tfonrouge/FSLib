package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.view.KVWebManager.configViewHome
import com.fonrouge.fsLib.view.ViewHome
import kotlin.reflect.KClass

abstract class ConfigViewHome<V : ViewHome<FILT>, FILT : ApiFilter>(
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
