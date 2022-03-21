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
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewLogin
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewItemPage()
        .onViewListPage()
        .on(viewHomeBase.configView.url, {
            clearHandleIntervalStack()
            viewHomeBase.dispatchActionPage()
        })
        .on(ViewLogin.configView.url, { runLoginPage() })
}

private fun Navigo.onViewItemPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/citem",
        { match ->
            configViewItemMap[match.data.dataClass as? String]?.let { configViewItem: ConfigViewItem<ViewItem<*, *>> ->
                configViewItem.viewFunc?.invoke(UrlParams(match))?.let { viewItem ->
                    val urlParams = viewItem.urlParams
                    when (urlParams?.action) {
                        ActionParam.Insert -> {
                            viewItem.dataContainer = null
                            viewItem.dispatchActionPage()
                        }
                        ActionParam.Update -> {
                            viewItem.dispatchActionPage()
                            configViewItem.updateData?.let { it(viewItem) }
                        }
                        else -> {
                            viewItem.dispatchActionPage()
                            configViewItem.updateData?.let { it(viewItem) }
                        }
                    }
                }
            }
        }
    )
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/clist",
        { match: Match ->
            configViewListMap[match.data.dataClass as String].let { listConfigView ->
                listConfigView?.let { KVWebManager.dispatchViewListPage(it, match) }
            }
        })
    return this
}
