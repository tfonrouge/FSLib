package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.IfceWebAction
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainerList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.security.AppScope
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewList
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.js.Date
import kotlin.reflect.KSuspendFunction1

class ConfigViewList<V : ViewList<T, U>, T : BaseModel<*>, U : BaseContainerList<T>>(
    name: String,
    label: String,
    val viewFunc: ((UrlParams?) -> V)? = null,
    val dataFunc: KSuspendFunction1<V, U?>,
    restUrl: String? = null,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : BaseConfigView(
    name = name,
    label = label,
    _restUrl = restUrl,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.CList
) {
    suspend fun updateData(urlParams: UrlParams?) {
        console.warn("urlParams =", urlParams)
        viewFunc?.invoke(urlParams)?.let { view ->
            var loading = if (!view.skipLoading) {
                KVWebManager.kvWebStore.dispatch(IfceWebAction.Loading(view))
                true
            } else {
                view.skipLoading = false
                false
            }
            val callBlock: () -> Unit = {
                try {
                    AppScope.launch {
                        dataFunc(view).let {
                            view.dataContainer = it as Nothing?
                            if (loading) {
                                loading = false
                                console.warn("launching dispatch")
                                KVWebManager.kvWebStore.dispatch(IfceWebAction.Loaded(view))
                            }
//                            block?.invoke(it)
                            view.displayBlock?.let { it() }
                        }
                    }
                } catch (e: Exception) {
                    console.warn("Error on interval =", e)
                }
            }
            if (view.repeatRefreshView == true) {
                var lastTime: Int? = null
                var lock = false
                ViewDataContainer.handleInterval = window.setInterval(
                    handler = {
                        val time = Date().getUTCSeconds()
                        if (lastTime != Date().getSeconds() && (time % KVWebManager.intervalTimeout == 0)) {
                            lastTime = Date().getUTCSeconds()
                            if (!lock) {
                                lock = true
                                callBlock()
                                lock = false
                            }
                        }
                    },
                    timeout = 250
                )
            }
            callBlock()
        }
    }
}
