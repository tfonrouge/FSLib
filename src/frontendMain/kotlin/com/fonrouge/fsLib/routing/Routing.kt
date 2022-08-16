package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewHome
import com.fonrouge.fsLib.apiLib.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.apiLib.KVWebManager.viewStateObservableValue
import com.fonrouge.fsLib.apiLib.ViewState
import com.fonrouge.fsLib.config.ConfigViewList.Companion.configViewListMap
import com.fonrouge.fsLib.config.dataUrlPrefix
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.CrudAction
import io.kvision.navigo.Match
import io.kvision.navigo.Navigo

fun Navigo.initialize(): Navigo {
    return this
        .onViewItemPage()
        .onViewListPage()
        .on("", {
            configViewHome?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            }
        })
}

private fun Navigo.onViewItemPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/item", { match ->
        configViewItemMap[match.data.dataClass as? String]?.let { configViewItem ->
            var urlParams = UrlParams(match = match)
            urlParams.action?.let { crudAction ->
                if (crudAction == CrudAction.Create) {
                    configViewItem.callItemService(
                        crudAction = crudAction,
                        callType = StateItem.CallType.Query,
                        contextDataUrl = urlParams.contextDataUrl
                    ) { itemContainer ->
                        console.warn("Navigo Create Query", itemContainer)
                        if (itemContainer.item != null) {
                            urlParams = UrlParams(
                                "action" to CrudAction.Update, "id" to itemContainer.item?._id
                            )
                            @Suppress("UNUSED_VARIABLE")
                            val url = (configViewItem.navigoUrl + urlParams.toString()).asDynamic()

                            @Suppress("UNUSED_VARIABLE")
                            val stateObj =
                                "{${itemContainer::class.simpleName}: \"${itemContainer.item?._id}\"}".asDynamic()
                            js("""history.replaceState(stateObj,"createToUpdate",url)""")
                            js("history.go(0)")
                            Unit
                        } else {
                            viewStateObservableValue.value = ViewState(configViewItem, urlParams)
                        }
                    }
                } else {
                    viewStateObservableValue.value = ViewState(configViewItem, urlParams)
                }
            }
        }
    })
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/list", { match: Match ->
        configViewListMap[match.data.dataClass as String]?.let { configViewList ->
            viewStateObservableValue.value = ViewState(configViewList, UrlParams(match = match))
        }
    })
    return this
}
