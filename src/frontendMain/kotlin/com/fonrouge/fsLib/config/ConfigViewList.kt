package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.ViewList
import kotlinx.serialization.json.JsonObject

class ConfigViewList<out V : ViewList<*, *>>(
    name: String,
    label: String,
    val viewFunc: ((UrlParams?) -> V)? = null,
    val updateData: ((@UnsafeVariance V) -> Unit)? = null,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView(
    name = name,
    label = label,
    _restUrl = restUrl,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.CList
)
