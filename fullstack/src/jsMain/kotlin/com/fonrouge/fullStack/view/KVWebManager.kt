@file:Suppress("unused")

package com.fonrouge.fullStack.view

import com.fonrouge.fullStack.config.ConfigView
import com.fonrouge.fullStack.config.ViewRegistry
import com.fonrouge.fullStack.routing.initialize
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

/**
 * Singleton object that manages the web application's configuration, routing, and other global settings.
 *
 * Inherits from CoroutineScope to provide structured concurrency with default background dispatcher and a supervisor job.
 */
object KVWebManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    lateinit var frontEndVersion: String
    lateinit var frontEndAppName: String
    var motto = "<motto>"
    var pageContainerWidth = "md"

    /**
     * The default view configuration to display when navigating to the root path.
     * If no view is registered with an empty base URL, this view will be used as fallback.
     */
    var defaultView: ConfigView<*, *>? = null

    /**
     * Map of item view configurations. Delegated to [ViewRegistry].
     */
    val configViewItemMap get() = ViewRegistry.configViewItemMap

    /**
     * Map of list view configurations. Delegated to [ViewRegistry].
     */
    val configViewListMap get() = ViewRegistry.configViewListMap

    /**
     * A flag indicating whether the periodic update for the data view item is enabled or not.
     * It is a mutable property that can be used to toggle the functionality as needed.
     * The default value is set to `true`.
     */
    var periodicUpdateDataViewItem = true

    /**
     * A variable indicating whether the periodic update for the data view list is enabled or not.
     *
     * If set to true, periodic updates are active; otherwise, updates are disabled.
     */
    var periodicUpdateDataViewList = true

    var viewStateObservableValue = ObservableValue<ViewState?>(null)

    var routing: Routing = Routing.init()

    fun initialize(block: (KVWebManager.() -> Unit)? = null) {

        block?.invoke(this)

        routing.initialize().resolve()
    }
}
