@file:Suppress("unused")

package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.config.IConfigView
import com.fonrouge.fsLib.routing.initialize
import io.kvision.routing.Routing
import io.kvision.routing.routing
import io.kvision.state.ObservableValue
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
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

    var configViewHome: ConfigViewHome<*>? = null

    private var authenticated = false

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

    fun initialize(block: (KVWebManager.() -> Unit)? = null) {

        block?.invoke(this)

//        Routing.init(root = null, useHash = true, strategy = Strategy.ONE)
        Routing.init()

        routing.initialize().resolve()

        if (iConfigView == null) {
            Toast.warning("${this::iConfigView.name} not implemented...")
        }

        afterInitialize?.invoke()
    }
}
