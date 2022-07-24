package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.lib.UrlParams
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.js.Date

abstract class ViewDataContainer<U : Any>(
    configView: ConfigViewContainer<*, *>,
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
        internal var handleInterval: Int? = null
            set(value) {
                field?.let {
                    window.clearInterval(it)
                }
                field = value
            }
    }

    val contextClassId get() = urlParams?.contextClassId

    var displayBlock: (() -> Unit)? = null
    val lookupParam get() = configView.lookupParam

    val name get() = configView.name

    abstract suspend fun singleUpdate()

    fun updateData(first: Boolean) {
        val callBlock = {
            AppScope.launch {
                try {
                    singleUpdate()
                } catch (e: Exception) {
                    console.error("Error on interval =", e)
                }
            }
        }
        if (repeatUpdateView == true) {
            var lastTime: Int? = null
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    val time = Date().getUTCSeconds()
                    if (lastTime != Date().getSeconds() && (time % repeatUpdateSecsInterval == 0)) {
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
        if (first) {
            callBlock()
        }
    }
}
