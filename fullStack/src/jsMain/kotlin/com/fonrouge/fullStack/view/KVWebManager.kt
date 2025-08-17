@file:Suppress("unused")

package com.fonrouge.fullStack.view

import com.fonrouge.fullStack.config.ConfigView
import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.config.ConfigViewList
import com.fonrouge.fullStack.routing.initialize
import com.fonrouge.base.config.IConfigView
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.toast.Toast
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

    var configViewHome: ConfigView<*, *, *>? = null
    val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()
    val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *, *>>()

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

    var iConfigView: IConfigView? = null

    var afterInitialize: (() -> Unit)? = null

    var routing: Routing = Routing.init()

    fun initialize(block: (KVWebManager.() -> Unit)? = null) {

        block?.invoke(this)

        routing.initialize().resolve()

        if (iConfigView == null) {
            Toast.warning("${this::iConfigView.name} not implemented...")
        }

        afterInitialize?.invoke()
    }
}
