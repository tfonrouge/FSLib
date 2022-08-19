package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.View
import kotlinx.serialization.json.JsonObject

private const val navigoPrefix = "#/"

abstract class ConfigView<V : View>(
    val name: String,
    val label: String,
    baseUrlPrefix: String = "view",
    baseUrlSuffix: String = "",
    val viewFunc: ((UrlParams?) -> V),
) {

    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*>>()
    }

    val url: String =
        baseUrlPrefix +
                (if (baseUrlPrefix.isEmpty()) "" else "/") +
                name +
                (if (baseUrlSuffix.isEmpty()) "" else "/") +
                baseUrlSuffix
    val navigoUrl: String = navigoPrefix + url
    val labelUrl: Pair<String, String> = label to navigoUrl

    init {
        if (this !is ConfigViewContainer<*, *>) {
            configViewMap[name] = this
        }
    }
}
