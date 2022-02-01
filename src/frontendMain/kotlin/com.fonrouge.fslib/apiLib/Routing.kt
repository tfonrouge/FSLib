package com.fonrouge.fslib.apiLib

import com.fonrouge.fslib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fslib.apiLib.KVWebManager.configViewListMap
import com.fonrouge.fslib.apiLib.KVWebManager.runLoginPage
import com.fonrouge.fslib.apiLib.KVWebManager.viewHomeBase
import com.fonrouge.fslib.config.ConfigViewItem
import com.fonrouge.fslib.config.dataUrlPrefix
import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.lib.UrlParams
import com.fonrouge.fslib.view.ViewDataContainer.Companion.clearHandleIntervalStack
import com.fonrouge.fslib.view.ViewItem
import com.fonrouge.fslib.view.ViewLogin
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
