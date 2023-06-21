package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewContainer
import io.kvision.state.ObservableValue
import io.kvision.utils.createInstance
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.js.Date

abstract class ViewDataContainer<FILT : Any>(
    val configViewContainer: ConfigViewContainer<*, *, *, FILT>,
    apiFilter: FILT?,
    editable: Boolean = true,
    icon: String? = null,
) : View(
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

    /**
     * observable that contains an [FILT] object. It can be assigned from an apiFilter= url parameter
     * or programmatically, and it's delivered to the backend
     */
    val apiFilter: ObservableValue<FILT> by lazy {
        ObservableValue(apiFilter ?: configViewContainer.apiFilterKClass.js.createInstance())
    }

    init {
        this.apiFilter.subscribe {
            onApiFilterUpdate()
        }
    }

    var displayBlock: (() -> Unit)? = null
    var suspendPeriodicUpdate = false

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
    open fun onApiFilterUpdate() {
        updateBanner()
        AppScope.launch { dataUpdate() }
    }

    override fun onBeforeDispose() {
        super.onBeforeDispose()
        handleInterval = null
    }

    /**
     * Sets the [apiFilter] value before display the view, By default tries to get the apiFilter value from the [urlParams]
     * 'apiFilter' param
     */
    @OptIn(InternalSerializationApi::class)
    open suspend fun setApiFilter() {
        urlParams?.pullUrlParam(
            serializer = configViewContainer.apiFilterKClass.serializer(),
            key = "apiFilter"
        )?.let {
            apiFilter.value = it
        }
    }
}
