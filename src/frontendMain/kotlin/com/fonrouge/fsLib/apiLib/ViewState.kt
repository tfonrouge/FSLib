package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container

class ViewState(
    private val configView: ConfigView<*>,
    private val urlParams: UrlParams?
) {
    @Suppress("unused")
    fun displayPage(container: Container) {
        return configView.viewFunc(urlParams).let { view ->
            view.onDisplayPage()
            view.displayPage(container)
        }
    }
}
