package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.ApiParam
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
    loading = loading,
    editable = editable,
    icon = icon,
    restUrlParams = restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    val name get() = configView.name

    val lookupParam get() = configView.lookupParam

    abstract var dataContainer: U?

    var onUpdateDataContainer: ((U?) -> Unit)? = null

    var displayBlock: (() -> Unit)? = null

    val contextClassId get() = urlParams?.contextClassId

    companion object {
        private val handleIntervalStack = mutableListOf<Int?>(null)
        var handleInterval: Int?
            get() = handleIntervalStack.last()
            set(value) {
                handleIntervalStack.last()?.let {
                    window.clearInterval(it)
                }
                handleIntervalStack[handleIntervalStack.lastIndex] = value
            }

        fun pushHandleInterval() {
            handleIntervalStack.add(null)
        }

        fun pullHandleInterval() {
            if (handleIntervalStack.size > 1) {
                handleIntervalStack.last()?.let { window.clearInterval(it) }
                handleIntervalStack.remove(handleIntervalStack.lastIndex)
            }
        }

        fun clearHandleIntervalStack() {
            handleIntervalStack.forEach {
                it?.let { window.clearInterval(it) }
            }
            handleIntervalStack.clear()
            handleIntervalStack.add(null)
        }
    }

    fun getApiParam(): ApiParam {
        return ApiParam()
    }

    fun updateData() {
        val callBlock: () -> Unit = {
            try {
                AppScope.launch {
//                    configView?.dataFunc?.invoke(getApiParam()).let {
/*
                    configView?.dataFunc?.invoke().let {
                        console.warn("dataFunc() view =", objId)
                        dataContainer = it as U?
                        if (loading) {
                            loading = false
                            KVWebManager.kvWebStore.dispatch(IfceWebAction.Loaded(this@ViewDataContainer))
                        }
                        //                            block?.invoke(it)
                        displayBlock?.let { it() }
                    }
*/
                }
            } catch (e: Exception) {
                console.warn("Error on interval =", e)
            }
        }
        if (repeatRefreshView == true) {
            var lastTime: Int? = null
            var lock = false
            handleInterval = window.setInterval(
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
