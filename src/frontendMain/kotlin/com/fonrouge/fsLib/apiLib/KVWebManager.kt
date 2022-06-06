@file:Suppress("unused")

package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.lib.withProgress
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

    val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *>>()

    private var authenticated = false

    const val intervalTimeout = 5
    var refreshViewItemPeriodic = false
    var refreshViewListPeriodic = false

    var setup: (KVWebManager.() -> Unit)? = null

    var observableConfigView = ObservableValue<ViewState?>(null)

    var iConfigView: IConfigView? = null

    var afterInitialize: (() -> Unit)? = null

    fun initialize() {

        setup?.invoke(this)

//        Routing.init(root = null, useHash = true, strategy = Strategy.ONE)
        Routing.init()

        routing.initialize().resolve()

        if (iConfigView == null) {
            Toast.warning("${this::iConfigView.name} not implemented...")
        }

        afterInitialize?.invoke()
    }

    internal fun showToastApiRemoteRequest(code: Int, title: String, message: String) {

        val msgDetail = if (code == 403) {
//            handleInterval = null
            authenticated = false
            """
<p>-<p>
The resource requires authentication which was not supplied with the request<br>
<b>Please Login
"""
        } else {
            ""
        }
        showToastApiError(title, message + msgDetail, 10000) {
            if (!authenticated) {
                routing.navigate("login")
            }
        }
    }

    fun showToastApiError(title: String, message: String, timeOut: Int = 5000, onHidden: (() -> Unit)? = null) {
        Toast.error(
            message = message,
            title = title,
            options = ToastOptions(
                positionClass = ToastPosition.BOTTOMFULLWIDTH,
                progressBar = true,
                closeButton = true,
                timeOut = timeOut,
                onHidden = onHidden,
            ),
        )
    }

//    fun dispatchViewListPage(
//        configViewList: ConfigViewList<*, *>,
//        match: Match
//    ) {
//        configViewList.dispatchViewPage(urlParams = UrlParams(match = match))
//    }
}
