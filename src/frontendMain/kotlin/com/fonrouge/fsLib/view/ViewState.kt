package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container
import io.kvision.html.div

class ViewState(
    val configView: ConfigView<*>,
    val urlParams: UrlParams?
)

@Suppress("unused")
fun Container.showView(viewState: ViewState) {
    val view = viewState.configView.newViewInstance(viewState.urlParams)
    view.apply {
        div {
            addBeforeDisposeHook {
                onBeforeDispose()
            }
            onBeforeDisplayPage(this@showView)
            this@showView.displayPage()
        }
    }
}
