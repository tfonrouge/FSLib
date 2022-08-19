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
) : View(
    configView = configView,
    editable = editable,
    icon = icon,
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

    var displayBlock: (() -> Unit)? = null

    val name get() = configView.name

    var suspendRepeatUpdate = false

    abstract suspend fun dataUpdate()

    fun installUpdate(first: Boolean) {
        val callBlock = {
            AppScope.launch {
                try {
                    dataUpdate()
                } catch (e: Exception) {
                    console.error("Error on interval =", e)
                }
            }
        }
        if (repeatUpdateView == true && !suspendRepeatUpdate) {
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
