package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.config.ConfigViewContainer
import kotlinx.browser.window
import kotlinx.coroutines.launch
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
        var startTime = 0L
        internal var handleInterval: Int? = null
            set(value) {
                field?.let {
                    window.clearInterval(it)
                }
                field = value
            }

        fun clearStartTime() {
            startTime = (Date().getTime() / 1000).toLong()
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
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    val curTime = (Date().getTime() / 1000).toLong()
                    if ((curTime - startTime) > repeatUpdateSecsInterval) {
                        if (!lock) {
                            startTime = curTime
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

    override fun onBeforeDispose() {
        super.onBeforeDispose()
        handleInterval = null
    }
}
