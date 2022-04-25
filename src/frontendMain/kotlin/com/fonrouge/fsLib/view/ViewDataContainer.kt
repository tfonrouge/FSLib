package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.ApiParam
import com.fonrouge.fsLib.AppScope
import com.fonrouge.fsLib.apiLib.IfceWebAction
import com.fonrouge.fsLib.apiLib.KVWebManager
import com.fonrouge.fsLib.config.BaseConfigView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.base.BaseContainer
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.js.Date

abstract class ViewDataContainer<U : BaseContainer>(
    configView: BaseConfigView<*, *>,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
//    actionPage: (View) -> IfceWebAction?,
    restUrlParams: UrlParams? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : View(
    configView = configView,
    loading = loading,
    editable = editable,
    icon = icon,
//    actionPage = actionPage,
    restUrlParams = restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {

    val name get() = configView?.name

    abstract var dataContainer: U?

    var displayBlock: (() -> Unit)? = null

    val contextClassId get() = urlParams?.contextClassId

    var skipLoading = false

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
        var loading = if (!skipLoading) {
            KVWebManager.kvWebStore.dispatch(IfceWebAction.Loading(this))
            true
        } else {
            skipLoading = false
            false
        }
        val callBlock: () -> Unit = {
            try {
                AppScope.launch {
                    configView?.dataFunc?.invoke(getApiParam()).let {
                        console.warn("dataFunc() view =", objId)
//                        dataContainer = it.unsafeCast<U?>()
                        dataContainer = it as U?
                        if (loading) {
                            loading = false
                            KVWebManager.kvWebStore.dispatch(IfceWebAction.Loaded(this@ViewDataContainer))
                        }
                        //                            block?.invoke(it)
                        displayBlock?.let { it() }
                    }
                }
            } catch (e: Exception) {
                console.warn("Error on interval =", e)
            }
        }
        if (repeatRefreshView == true) {
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
