package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container

class ViewState(
    val configView: ConfigView<*>,
    val urlParams: UrlParams?
)

@Suppress("unused")
fun Container.showView(viewState: ViewState) {
    viewState.configView.viewFunc(viewState.urlParams).apply {
        onBeforeDisplayPage()
        displayPage(this@showView)
    }
}
