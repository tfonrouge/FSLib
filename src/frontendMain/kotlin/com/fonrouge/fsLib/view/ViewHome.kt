package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import kotlinx.serialization.json.JsonObject

abstract class ViewHome(
    configView: ConfigViewHome<*>,
    editable: Boolean = true,
    icon: String? = null,
) : View(
    configView = configView,
    editable = editable,
    icon = icon,
) {
    init {
        caption = "Home"
    }
}
