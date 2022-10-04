package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.utils.createInstance

class ViewState(
    val configView: ConfigView<*>,
    val urlParams: UrlParams?
)

@Suppress("unused")
fun Container.showView(viewState: ViewState) {
    viewState.configView.viewFunc.js.createInstance<View>(viewState.urlParams).apply {
        div {
            addBeforeDisposeHook {
                onBeforeDispose()
            }
            onBeforeDisplayPage(this@showView)
            this@showView.displayPage()
        }
    }
}
