package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.ViewHome

abstract class ConfigViewHome<V : ViewHome>(
    name: String,
    label: String,
    baseUrlPrefix: String = "",
    viewFunc: (UrlParams?) -> V,
) : ConfigView<ViewHome>(
    name = name,
    label = label,
    baseUrlPrefix = baseUrlPrefix,
    viewFunc = viewFunc,
) {
    init {
        configViewHome = this
    }
}
