package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.ViewHome
import kotlinx.serialization.json.JsonObject

abstract class ConfigViewHome<V : ViewHome>(
    name: String,
    label: String,
    typeView: TypeView = TypeView.None,
    url: String = "$name${typeView.label}",
    restUrlParams: UrlParams? = null,
    viewFunc: (UrlParams?) -> V,
    lookupParam: JsonObject? = null
) : ConfigView<ViewHome>(
    name = name,
    label = label,
    typeView = typeView,
    url = url,
    restUrlParams = restUrlParams,
    viewFunc = viewFunc,
    lookupParam = lookupParam
) {
    init {
        configViewHome = this
    }
}
