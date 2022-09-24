package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.KVWebManager.viewStateObservableValue
import com.fonrouge.fsLib.apiLib.ViewState
import com.fonrouge.fsLib.config.ConfigView.Companion.configViewMap
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList.Companion.configViewListMap
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewPage()
        .on(configViewHome?.baseUrl ?: "", {
            configViewHome?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            }
        })
}

private fun Navigo.onViewPage(): Navigo {
    on(":viewClass", { match ->
        val route = match.data.viewClass
        configViewMap[route as? String]?.let { configView ->
            viewStateObservableValue.value = ViewState(configView, UrlParams(match))
        }
        configViewItemMap[route as? String]?.let { configViewItem ->
            viewStateObservableValue.value = ViewState(configViewItem, UrlParams(match = match))
        }
        configViewListMap[match.data.viewClass as String]?.let { configViewList ->
            viewStateObservableValue.value = ViewState(configViewList, UrlParams(match = match))
        }
    })
    return this
}
