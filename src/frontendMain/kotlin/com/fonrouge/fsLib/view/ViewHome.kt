package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.lib.UrlParams

abstract class ViewHome(
    urlParams: UrlParams? = null,
    configView: ConfigViewHome<*>,
    editable: Boolean = true,
    icon: String? = null,
) : View(
    urlParams = urlParams,
    configView = configView,
    editable = editable,
    icon = icon,
    label = "Home"
)
