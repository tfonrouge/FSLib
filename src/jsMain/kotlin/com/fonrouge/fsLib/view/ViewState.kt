package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.state.bind

class ViewState(
    val configView: ConfigView<*, *, *>,
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
            bind(view.apiFilterObservable) {
                view.onApiFilterUpdate()
                view.apiFilterToUrl()
            }
            onAfterDisplayPage()
        }
    }
}
