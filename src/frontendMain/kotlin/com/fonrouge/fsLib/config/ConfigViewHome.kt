package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.view.ViewHome
import kotlin.reflect.KClass

abstract class ConfigViewHome<V : ViewHome>(
    name: String,
    label: String,
    viewFunc: KClass<V>,
    baseUrl: String = "",
) : ConfigView<ViewHome>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
) {
    init {
        configViewHome = this
    }
}
