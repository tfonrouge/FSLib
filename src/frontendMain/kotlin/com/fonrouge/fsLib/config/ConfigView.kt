package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.view.View
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

abstract class ConfigView<V : View>(
    val name: String,
    val label: String,
    val viewFunc: KClass<out V>,
    val baseUrl: String = viewFunc.js.name
) {
    companion object {
        val configViewMap = mutableMapOf<String, ConfigView<*>>()
    }

    val url: String = navigoPrefix + this.baseUrl
    val labelUrl: Pair<String, String> = label to url

    init {
        if (this !is ConfigViewContainer<*, *>) {
            configViewMap[baseUrl] = this
        }
    }
}
