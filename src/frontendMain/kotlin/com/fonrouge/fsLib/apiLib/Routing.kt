package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewListMap
import com.fonrouge.fsLib.apiLib.KVWebManager.runLoginPage
import com.fonrouge.fsLib.apiLib.KVWebManager.viewHomeBase
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.dataUrlPrefix
import com.fonrouge.fsLib.lib.ActionParam
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
            viewHomeBase.dispatchActionPage()
        })
        .on("login", { runLoginPage() })
}

private fun Navigo.onViewItemPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/citem",
        { match ->
            configViewItemMap[match.data.dataClass as? String]?.let { configViewItem: ConfigViewItem<*, *, *> ->
                configViewItem.dispatchViewPage(urlParams = UrlParams(match = match))
            }
        }
    )
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/clist",
        { match: Match ->
            configViewListMap[match.data.dataClass as String].let { listConfigView ->
                listConfigView?.let {
                    KVWebManager.dispatchViewListPage(it, match)
                }
            }
        })
    return this
}
