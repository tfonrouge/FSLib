package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import kotlinx.serialization.json.JsonObject

open class ConfigViewList<T : BaseModel<*>, V : ViewList<T, *>>(
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
    typeView = TypeView.CList,
    viewFunc = viewFunc,
) {

    init {
        KVWebManager.configViewListMap[name] = this
    }
}
