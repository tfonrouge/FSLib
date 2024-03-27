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
    console.warn("1111111111111111")
    val view = viewState.configView.newViewInstance(viewState.urlParams)
    console.warn("2222222222222222")
    view.apply {
        div {
            addBeforeDisposeHook {
                onBeforeDispose()
            }
            onBeforeDisplayPage(this@showView)
            this@showView.displayPage()
            bind(view.apiFilterObservableValue) {
                view.onApiFilterUpdate()
                view.apiFilterToUrl()
            }
            onAfterDisplayPage()
        }
    }
}
