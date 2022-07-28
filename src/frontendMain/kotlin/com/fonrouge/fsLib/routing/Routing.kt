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
import kotlinx.browser.window

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
    on("$dataUrlPrefix/:dataClass/item",
        { match ->
            configViewItemMap[match.data.dataClass as? String]?.let { configViewItem ->
                var urlParams = UrlParams(match = match)
                urlParams.action?.let { crudAction ->
                    if (crudAction == CrudAction.Create) {
                        configViewItem.callItemService(
                            crudAction = crudAction,
                            callType = StateItem.CallType.Query,
                        ) {
                            console.warn("Navigo Create Query", it)
                            if (it.item != null) {
                                urlParams = UrlParams(
                                    "action" to CrudAction.Update,
                                    "id" to it.item._id
                                )
                                var url = (configViewItem.url + urlParams.toString()).asDynamic()
                                val stateObj = "{${it::class.simpleName}: \"${it.item._id}\"}".asDynamic()
                                console.warn("replaceState", stateObj, url)
                                js("""history.replaceState(stateObj,"createToUpdate",url)""")
                                url = configViewItem.url + urlParams.toString()
//                                navigateByName()
                                console.warn("navigate", url)
                                window.setTimeout(
                                    handler = {
                                        navigate(url)
                                    },
                                    timeout = 1000
                                )
                            } else {
                                viewStateObservableValue.value = ViewState(configViewItem, urlParams)
                            }
                        }
                    } else {
                        viewStateObservableValue.value = ViewState(configViewItem, urlParams)
                    }
                }
            }
        }
    )
    return this
}

private fun Navigo.onViewListPage(): Navigo {
    on("$dataUrlPrefix/:dataClass/list",
        { match: Match ->
            configViewListMap[match.data.dataClass as String]?.let { configViewList ->
                viewStateObservableValue.value = ViewState(configViewList, UrlParams(match = match))
            }
        })
    return this
}
