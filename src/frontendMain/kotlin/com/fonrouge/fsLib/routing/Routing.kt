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
            var urlParams = UrlParams(match = match)
            urlParams.crudAction?.let { crudAction ->
                if (crudAction == CrudAction.Create) {
                    configViewItem.callItemService(
                        crudAction = crudAction,
                        callType = StateItem.CallType.Query,
                        contextDataUrl = urlParams.contextDataUrl
                    ) { itemContainer ->
                        if (itemContainer.itemAlreadyOn) {
                            urlParams = UrlParams(
                                "action" to CrudAction.Update, "id" to JSON.stringify(itemContainer.item?._id)
                            )
                            @Suppress("UNUSED_VARIABLE")
                            val url = (configViewItem.navigoUrl + urlParams.toString()).asDynamic()

                            @Suppress("UNUSED_VARIABLE")
                            val stateObj =
                                "{${itemContainer::class.simpleName}: \"${itemContainer.item?._id}\"}".asDynamic()
                            js("""history.replaceState(stateObj,"createToUpdate",url)""")
                            js("history.go(0)")
                            return@callItemService
                        }
                    }
                }
            }
            viewStateObservableValue.value = ViewState(configViewItem, urlParams)
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
