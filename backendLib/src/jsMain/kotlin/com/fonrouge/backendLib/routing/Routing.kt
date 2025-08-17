package com.fonrouge.backendLib.routing

import com.fonrouge.backendLib.config.ConfigView.Companion.configViewMap
import com.fonrouge.backendLib.view.KVWebManager.configViewHome
import com.fonrouge.backendLib.view.KVWebManager.configViewItemMap
import com.fonrouge.backendLib.view.KVWebManager.configViewListMap
import com.fonrouge.backendLib.view.KVWebManager.viewStateObservableValue
import com.fonrouge.backendLib.view.ViewState
import com.fonrouge.fsLib.lib.UrlParams
import io.kvision.navigo.Navigo

/**
 * Initializes the `Navigo` instance and sets up routes, including the home route and view-specific routes.
 *
 * This method configures the navigator to handle specific paths and their associated actions. When invoked,
 * it sets up a state for the view, updating the `viewStateObservableValue` based on the matched route configuration.
 *
 * @return The `Navigo` instance after initialization, allowing for method chaining.
 */
fun Navigo.initialize(): Navigo {
    return this
        .onViewPage()
        .on(configViewHome?.baseUrl ?: "", {
            configViewHome?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            }
        })
}

/**
 * Registers a route in the `Navigo` instance to handle dynamic view-specific paths.
 * Updates the `viewStateObservableValue` with a new `ViewState` that contains the appropriate
 * configuration and URL parameters based on the matched route.
 *
 * The method dynamically identifies the configuration type (view, item, or list)
 * for the given route and initializes the state accordingly.
 *
 * @return The modified `Navigo` instance to allow method chaining.
 */
private fun Navigo.onViewPage(): Navigo {
    on(
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
    return this
}
