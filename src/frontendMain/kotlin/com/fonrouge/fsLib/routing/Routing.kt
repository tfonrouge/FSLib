package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.KVWebManager.viewStateObservableValue
import com.fonrouge.fsLib.apiLib.ViewState
import com.fonrouge.fsLib.config.ConfigView.Companion.configViewMap
import com.fonrouge.fsLib.config.ConfigViewItem.Companion.configViewItemMap
import com.fonrouge.fsLib.config.ConfigViewList.Companion.configViewListMap
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewPage()
        .onViewItemPage()
        .onViewListPage()
        .on("", {
            configViewHome?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            }
        })
}

private fun Navigo.onViewPage(): Navigo {
    on("view/:name", { match ->
        configViewMap[match.data.name as? String]?.let { configView ->
            viewStateObservableValue.value = ViewState(configView, UrlParams(match))
        }
    })
    return this
}

private fun Navigo.onViewItemPage(): Navigo {
    on("data/:dataClass/item", { match ->
        configViewItemMap[match.data.dataClass as? String]?.let { configViewItem ->
            viewStateObservableValue.value = ViewState(configViewItem, UrlParams(match = match))
        }
    })
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("data/:dataClass/list", { match: Match ->
        configViewListMap[match.data.dataClass as String]?.let { configViewList ->
            viewStateObservableValue.value = ViewState(configViewList, UrlParams(match = match))
        }
    })
    return this
}
