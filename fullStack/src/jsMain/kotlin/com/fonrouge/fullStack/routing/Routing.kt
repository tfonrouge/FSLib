package com.fonrouge.fullStack.routing

import com.fonrouge.base.lib.UrlParams
import com.fonrouge.fullStack.config.ConfigView.Companion.configViewMap
import com.fonrouge.fullStack.view.KVWebManager.configViewItemMap
import com.fonrouge.fullStack.view.KVWebManager.configViewListMap
import com.fonrouge.fullStack.view.KVWebManager.viewStateObservableValue
import com.fonrouge.fullStack.view.ViewState
import io.kvision.navigo.Navigo

/**
 * Initializes the Navigo router with predefined route behaviors, setting view states
 * based on configurations and URL parameters.
 *
 * This method sets up two routes:
 * - A default route triggered when the path is empty, which attempts to set the view state
 *   using the configuration views (`configViewMap`, `configViewItemMap`, or `configViewListMap`)
 *   associated with an empty path.
 * - A route with a dynamic `:viewClass` path segment, which updates the view state based on
 *   the corresponding configuration views and the parsed URL parameters.
 *
 * If no configuration is found for an empty path, a warning is logged to the console.
 *
 * @return The initialized instance of Navigo router for chaining method calls.
 */
fun Navigo.initialize(): Navigo {
    return this
        .on({
            (configViewMap[""] ?: configViewItemMap[""] ?: configViewListMap[""])?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            } ?: run {
                console.warn("no configView defined to empty path")
            }
        })
        .on(
            path = ":viewClass",
            f = { match ->
                val route = match.data.viewClass
                configViewMap[route as? String]?.let { configView ->
                    viewStateObservableValue.value = ViewState(configView, UrlParams(match))
                }
                configViewItemMap[route as? String]?.let { configViewItem ->
                    viewStateObservableValue.value = ViewState(configViewItem, UrlParams(match = match))
                }
                configViewListMap[route as? String]?.let { configViewList ->
                    viewStateObservableValue.value = ViewState(configViewList, UrlParams(match = match))
                }
            }
        )
}
