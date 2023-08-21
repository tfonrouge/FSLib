@file:Suppress("unused")

package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.config.IConfigView
import com.fonrouge.fsLib.routing.initialize
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.toast.Toast
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

object KVWebManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    lateinit var frontEndVersion: String
    lateinit var frontEndAppName: String
    var motto = "<motto>"
    var pageContainerWidth = "md"

    var configViewHome: ConfigViewHome<*, *>? = null

    /**
     * Global variable to allow periodic update on [ViewItem]
     * Set to true if periodic update is allowed
     */
    var periodicUpdateDataViewItem = false

    /**
     * Global variable to allow periodic update on [ViewList]
     * Set to true if periodic update is allowed
     */
    var periodicUpdateDataViewList = false

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
