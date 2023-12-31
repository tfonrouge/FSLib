package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlin.js.Date

abstract class ViewDataContainer<FILT : IApiFilter>(
    urlParams: UrlParams?,
    val configViewContainer: ConfigViewContainer<*, *, *, FILT>,
    editable: Boolean = true,
    icon: String? = null,
) : View<FILT>(
    urlParams = urlParams,
    configView = configViewContainer,
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

    @Suppress("MemberVisibilityCanBePrivate")
    var suspendPeriodicUpdate = false
    abstract suspend fun dataUpdate()

    open suspend fun onDataUpdate() {
        dataUpdate()
    }

    fun installUpdate(first: Boolean) {
        val callBlock = {
            AppScope.launch {
                try {
                    onDataUpdate()
                } catch (e: Exception) {
                    console.error("Error on interval =", e)
                }
            }
        }
        if (periodicUpdateDataView == true && !suspendPeriodicUpdate) {
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    val curTime = (Date().getTime() / 1000).toLong()
                    if ((curTime - startTime) > periodicUpdateViewInterval) {
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

    /**
     * open function that allows to override the default action when the [apiFilter] observable changes.
     * The default action will do an [updateBanner] and then an [dataUpdate]
     */
    override fun onApiFilterUpdate() {
        super.onApiFilterUpdate()
        AppScope.launch { dataUpdate() }
    }

    override fun onBeforeDispose() {
        super.onBeforeDispose()
        handleInterval = null
    }
}
