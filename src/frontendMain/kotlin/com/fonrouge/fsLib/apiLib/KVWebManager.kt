@file:Suppress("unused")

package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.config.ConfigViewHome
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.withProgress
import com.fonrouge.fsLib.model.MediaItem
import com.fonrouge.fsLib.routing.*
import io.kvision.navigo.Match
import io.kvision.redux.ReduxStore
import io.kvision.redux.createReduxStore
import io.kvision.routing.Routing
import io.kvision.routing.Strategy
import io.kvision.routing.routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object KVWebManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    lateinit var frontEndVersion: String
    lateinit var frontEndAppName: String
    var motto = "<motto>"
    var pageContainerWidth = "md"

    lateinit var kvWebStore: ReduxStore<KVWebState, KVAction>
    var configViewHome: ConfigViewHome<*>? = null
        set(value) {
            field = value
            value?.viewFunc?.invoke(null)?.dispatchActionPage()
        }

    val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *>>()

    val state get() = kvWebStore.getState()

    private var authenticated = false

    const val intervalTimeout = 5
    var refreshViewItemPeriodic = false
    var refreshViewListPeriodic = false

    var setup: (KVWebManager.() -> Unit)? = null

    fun initialize() {

        setup?.invoke(this)

        kvWebStore = createReduxStore(::reducer, KVWebState())

        Routing.init(root = null, useHash = true, strategy = Strategy.ONE)

        routing.initialize().resolve()

        afterInitialize()
    }

    private fun afterInitialize() {
        kvWebStore.dispatch(IfceWebAction.AppLoaded)
    }

    fun getMediaList(item: Pair<String, *>, context: String, f: (List<MediaItem>?) -> Unit) {
        withProgress {
/*
            val list = restCall<List<MediaItem>>(
                url = "${Api.API_BASE_URL}media/url/${item.first}/${item.second}/$context",
                method = HttpMethod.GET,
                headers = headers(true)
            )
            list?.let {
                f(list)
            }
*/
        }
    }

    fun runLoginPage() {
        kvWebStore.dispatch(IfceWebAction.LoginPage)
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

    fun dispatchViewListPage(
        configViewList: ConfigViewList<*, *>,
        match: Match
    ) {
        configViewList.dispatchViewPage(urlParams = UrlParams(match = match))
    }
}
