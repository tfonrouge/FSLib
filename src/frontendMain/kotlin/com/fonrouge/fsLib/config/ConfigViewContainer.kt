package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.json.JsonObject

const val dataUrlPrefix = "data"

abstract class ConfigViewContainer<T : BaseModel<*>, V : ViewDataContainer<*>>(
    name: String,
    label: String,
    typeView: TypeView,
    url: String = "$dataUrlPrefix/$name${typeView.label}",
    restUrlParams: UrlParams? = null,
    viewFunc: ((UrlParams?) -> V),
    lookupParam: JsonObject? = null,
) : ConfigView<V>(
    name = name,
    label = label,
    typeView = typeView,
    url = url,
    restUrlParams = restUrlParams,
    viewFunc = viewFunc,
    lookupParam = lookupParam
) {

    final override fun urlTyped(typeView: TypeView): String {
        return "$dataUrlPrefix/$name${typeView.label}"
    }

    val labelUrl get() = label to navigoUrl
}
