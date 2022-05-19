package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container

class ViewState(
    val configView: ConfigView<*>,
    val urlParams: UrlParams?
) {
    fun displayPage(container: Container) {
        return configView.viewFunc(urlParams).displayPage(container)
    }
}
