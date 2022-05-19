package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.View
import kotlinx.serialization.json.JsonObject

private const val navigoPrefix = "#/"

abstract class ConfigView<V : View>(
    val name: String,
    val label: String,
    val typeView: TypeView = TypeView.None,
    val url: String = "$name${typeView.label}",
    val restUrlParams: UrlParams? = null,
    val viewFunc: ((UrlParams?) -> V),
    val lookupParam: JsonObject? = null
) {

    val navigoUrl: String = navigoPrefix + url

    open fun urlTyped(typeView: TypeView): String {
        return "/$name${typeView.label}"
    }
}
