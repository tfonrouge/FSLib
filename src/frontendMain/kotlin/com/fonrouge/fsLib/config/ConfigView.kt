package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.view.View
import kotlin.reflect.KClass

private const val navigoPrefix = "#/"

/*
    TODO: encode/decode baseUrl to be url compliant
 */
abstract class ConfigView<V : View>(
    val name: String,
    val label: String,
    val viewFunc: KClass<out V>,
    val baseUrl: String = viewFunc.simpleName!!
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
