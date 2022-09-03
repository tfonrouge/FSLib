package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.View

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

    val urlWithoutNavigoPrefix: String =
        baseUrlPrefix +
                (if (baseUrlPrefix.isEmpty()) "" else "/") +
                name +
                (if (baseUrlSuffix.isEmpty()) "" else "/") +
                baseUrlSuffix
    val url: String = navigoPrefix + urlWithoutNavigoPrefix
    val labelUrl: Pair<String, String> = label to url

    init {
        if (this !is ConfigViewContainer<*, *>) {
            configViewMap[name] = this
        }
    }
}
