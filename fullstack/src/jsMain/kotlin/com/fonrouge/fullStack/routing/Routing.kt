package com.fonrouge.fullStack.routing

import com.fonrouge.fullStack.lib.UrlParams
import com.fonrouge.fullStack.config.ViewRegistry
import com.fonrouge.fullStack.view.KVWebManager
import com.fonrouge.fullStack.view.KVWebManager.viewStateObservableValue
import com.fonrouge.fullStack.view.ViewState
import io.kvision.navigo.Navigo

/**
 * Initializes the Navigo router with predefined route behaviors, setting view states
 * based on configurations and URL parameters.
 *
 * Sets up two routes:
 * - A default route (empty path) that resolves the view from [ViewRegistry] by empty key,
 *   falling back to [KVWebManager.defaultView] if no view is registered for the empty path.
 * - A dynamic `:viewClass` route that finds the matching view in [ViewRegistry].
 *
 * @return The initialized Navigo instance for method chaining.
 */
fun Navigo.initialize(): Navigo {
    return this
        .on({
            val configView = ViewRegistry.findByUrl("") ?: KVWebManager.defaultView
            if (configView != null) {
                viewStateObservableValue.value = ViewState(configView, UrlParams())
            } else {
                console.warn("No default view configured. Set KVWebManager.defaultView in your initialize block.")
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
