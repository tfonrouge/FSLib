package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlin.js.Date

/**
 * An abstract class `ViewDataContainer` which extends from the `View`. This class is designed
 * to manage the configuration and periodic update of a view container.
 *
 * @param CC The type of the common container, must extend from `ICommonContainer`.
 * @param T The type of the data item, must extend from `BaseDoc`.
 * @param ID The type of the ID of data item, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, must extend `IApiFilter`.
 * @property configViewContainer The configuration object for the view container.
 */
abstract class ViewDataContainer<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val configViewContainer: ConfigViewContainer<CC, T, ID, *, FILT>,
) : View<CC, FILT>(
    configView = configViewContainer,
) {
    companion object {
        var startTime = 0L
        var handleInterval: Int? = null
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
                    if (this@ViewDataContainer is ViewList<*, *, *, *, *>) {
                        (this@ViewDataContainer as ViewList<*, *, *, *, *>).tabulator?.onIntervalUpdate?.invoke()
                    }
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
     * open function that allows to override the default action when the [apiFilterObservable] observable changes.
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
