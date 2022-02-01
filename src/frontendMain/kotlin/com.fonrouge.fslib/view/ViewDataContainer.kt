package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.IfceWebAction
import com.fonrouge.fslib.config.BaseConfigView
import com.fonrouge.fslib.lib.UrlParams
import com.fonrouge.fslib.model.base.BaseContainer
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject

abstract class ViewDataContainer<U : BaseContainer>(
    val name: String,
    configView: BaseConfigView,
    loading: Boolean = false,
    editable: Boolean = true,
    icon: String? = null,
    actionPage: (View) -> IfceWebAction?,
    restUrlParams: UrlParams? = null,
    matchFilterParam: JsonObject? = null,
    sortParam: JsonObject? = null,
) : View(
    configView = configView,
    loading = loading,
    editable = editable,
    icon = icon,
    actionPage = actionPage,
    restUrlParams = restUrlParams,
    matchFilterParam = matchFilterParam,
    sortParam = sortParam
) {
    open var dataContainer: U? = null

    var displayBlock: (() -> Unit)? = null

    val contextPair get() = urlParams?.contextPair

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
/*
    inline fun <reified W : BaseModel> getContextItem(crossinline block: (W?) -> Unit) {
        urlParams?.contextPair?.let { contextPair ->
            KVWebManager.restContainerItem(
                contextPair.first,
                contextPair.second,
                function = block)
        }
    }
*/
}
