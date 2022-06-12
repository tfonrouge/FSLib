package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.apiLib.KVWebManager.observableConfigView
import com.fonrouge.fsLib.apiLib.ViewState
import com.fonrouge.fsLib.config.ConfigViewList.Companion.configViewListMap
import com.fonrouge.fsLib.config.dataUrlPrefix
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewItemPage()
        .onViewListPage()
        .on("", {
            configViewHome?.let {
                observableConfigView.value = ViewState(it, UrlParams())
            }
        })
}

private fun Navigo.onViewItemPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/item",
        { match ->
            configViewItemMap[match.data.dataClass as? String]?.let { configViewItem ->
                observableConfigView.value = ViewState(configViewItem, UrlParams(match = match))
            }
        }
    )
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/list",
        { match: Match ->
            configViewListMap[match.data.dataClass as String]?.let { configViewList ->
                observableConfigView.value = ViewState(configViewList, UrlParams(match = match))
            }
        })
    return this
}
