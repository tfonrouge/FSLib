package com.fonrouge.fullStack.routing

import com.fonrouge.base.lib.UrlParams
import com.fonrouge.fullStack.config.ViewRegistry
import com.fonrouge.fullStack.view.KVWebManager.viewStateObservableValue
import com.fonrouge.fullStack.view.ViewState
import io.kvision.navigo.Navigo

/**
 * Initializes the Navigo router with predefined route behaviors, setting view states
 * based on configurations and URL parameters.
 *
 * Sets up two routes:
 * - A default route (empty path) that looks up the view in [ViewRegistry].
 * - A dynamic `:viewClass` route that finds the matching view in [ViewRegistry].
 *
 * If no configuration is found for the empty path, a warning is logged to the console.
 *
 * @return The initialized Navigo instance for method chaining.
 */
fun Navigo.initialize(): Navigo {
    return this
        .on({
            ViewRegistry.findByUrl("")?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            } ?: run {
                console.warn("no configView defined to empty path")
            }
        })
        .on(
            path = ":viewClass",
            f = { match ->
                val route = match.data.viewClass as? String ?: return@on
                ViewRegistry.findByUrl(route)?.let { configView ->
                    viewStateObservableValue.value = ViewState(configView, UrlParams(match))
                }
            }
        )
}
