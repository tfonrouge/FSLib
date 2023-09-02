package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.KVWebManager.getSysUser
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.state.bind

class ViewState(
    val configView: ConfigView<*, *>,
    val urlParams: UrlParams?
)

@Suppress("unused")
suspend fun Container.showView(viewState: ViewState) {
    getSysUser?.let { it() }
    val view = viewState.configView.newViewInstance(viewState.urlParams)
    val viewDataContainer = view as? ViewDataContainer<*>
    view.apply {
        div {
            addBeforeDisposeHook {
                onBeforeDispose()
            }
            onBeforeDisplayPage(this@showView)
            this@showView.displayPage()
            viewDataContainer?.let {
                bind(viewDataContainer.apiFilter) {
                    viewDataContainer.onApiFilterUpdate()
                    viewDataContainer.apiFilterToUrl()
                }
            }
            onAfterDisplayPage()
        }
    }
}
