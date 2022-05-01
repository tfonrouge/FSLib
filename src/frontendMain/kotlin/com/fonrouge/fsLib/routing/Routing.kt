package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.apiLib.KVWebManager.runLoginPage
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList.Companion.configViewListMap
import com.fonrouge.fsLib.config.dataUrlPrefix
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.view.ViewDataContainer.Companion.clearHandleIntervalStack
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewItemPage()
        .onViewListPage()
        .on("", {
            clearHandleIntervalStack()
            configViewHome?.viewFunc?.invoke(null)?.dispatchActionPage()
        })
        .on("login", { runLoginPage() })
}

private fun Navigo.onViewItemPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/item",
        { match ->
            configViewItemMap[match.data.dataClass as? String]?.let { configViewItem: ConfigViewItem<*, *> ->
                configViewItem.dispatchViewPage(urlParams = UrlParams(match = match))
            }
        }
    )
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/list",
        { match: Match ->
            console.warn("onViewListPage match", match, configViewListMap, configViewListMap.size)
            configViewListMap.forEach {
                console.warn("${it.key} -> ${it.value}")
            }
            console.warn("onViewListPage match 2", match, configViewListMap, configViewListMap.size)
            configViewListMap[match.data.dataClass as String].let { listConfigView ->
                listConfigView?.let {
                    console.warn("dispatchViewListPage onViewListPage match", match)
                    KVWebManager.dispatchViewListPage(it, match)
                }
            }
        })
    return this
}
