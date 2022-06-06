package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.lib.UrlParams
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.js.Date

abstract class ViewDataContainer<U : Any>(
    configView: ConfigViewContainer<*, *>,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    restUrlParams: UrlParams? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : View(
    configView = configView,
    editable = editable,
    icon = icon,
    restUrlParams = restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    companion object {
        private var handleInterval: Int? = null
            set(value) {
                console.warn("HANDLEINTERVAL set(value)", value)
                field?.let {
                    console.warn("CLEARING INTERVAL", it)
                    window.clearInterval(it)
                }
                field = value
            }
    }

    val name get() = configView.name

    val lookupParam get() = configView.lookupParam

//    abstract var dataContainer: ObservableValue<U>?

    var displayBlock: (() -> Unit)? = null

    val contextClassId get() = urlParams?.contextClassId

    abstract suspend fun callUpdate()

    fun updateData() {
        val callBlock: () -> Unit = {
            try {
                AppScope.launch {
                    callUpdate()
                }
            } catch (e: Exception) {
                console.warn("Error on interval =", e)
            }
        }
        if (repeatUpdateView == true) {
            var lastTime: Int? = null
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    console.warn("entering HANDLE", handleInterval)
                    val time = Date().getUTCSeconds()
                    if (lastTime != Date().getSeconds() && (time % repeatUpdateSecsInterval == 0)) {
                        console.warn("updating HANDLE")
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
            console.warn("INSTALLING REPEAT REFRESH", this, "INTERVAL", handleInterval)
        }
        callBlock()
    }
}
