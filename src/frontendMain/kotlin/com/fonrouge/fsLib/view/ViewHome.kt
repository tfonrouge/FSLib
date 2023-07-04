package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.ApiFilter

abstract class ViewHome<FILT : ApiFilter>(
    urlParams: UrlParams? = null,
    configView: ConfigViewHome<*, FILT>,
    editable: Boolean = true,
    icon: String? = null,
) : View<FILT>(
    urlParams = urlParams,
    configView = configView,
    editable = editable,
    icon = icon,
    label = "Home"
)
