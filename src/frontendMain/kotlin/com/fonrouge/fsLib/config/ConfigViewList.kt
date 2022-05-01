package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import kotlinx.serialization.json.JsonObject

abstract class ConfigViewList<T : BaseModel<*>, V : ViewList<T, *>>(
    name: String,
    label: String,
    viewFunc: ((UrlParams?) -> V),
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : ConfigViewContainer<T, V>(
    name = name,
    label = label,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.List,
    viewFunc = viewFunc,
) {

    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *>>()
    }

    init {
        configViewListMap[name] = this
        console.warn("adding configViewListMap item", name, configViewListMap.map { it.key })
    }
}
