package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.apiData.ApiFilter
import io.kvision.state.ObservableValue
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.js.Date

abstract class ViewDataContainer<FILT : ApiFilter>(
    urlParams: UrlParams?,
    val configViewContainer: ConfigViewContainer<*, *, *, FILT>,
    editable: Boolean = true,
    icon: String? = null,
) : View(
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

    /**
     * observable that contains an [FILT] object. It can be assigned from an apiFilter= url parameter
     * or programmatically, and it's delivered to the backend
     */
    val apiFilter: ObservableValue<FILT> by lazy {
        ObservableValue(apiFilterFromUrl ?: newApiFilterInstance())
    }

    @OptIn(InternalSerializationApi::class)
    private val apiFilterFromUrl: FILT?
        get() = urlParams?.pullUrlParam(
            serializer = configViewContainer.apiFilterKClass.serializer(),
            key = "apiFilter"
        )

    override fun onAfterDisplayPage() {
        super.onAfterDisplayPage()
        if (apiFilterFromUrl == null)
            AppScope.launch {
                initialApiFilter()?.let {
                    apiFilter.value = it
                }
            }
    }

    /**
     * Sets the current browser url with an [apiFilter] url parameter
     */
    @OptIn(InternalSerializationApi::class)
    fun apiFilterToUrl() {
        val pair = configView.pairParam("apiFilter", configViewContainer.apiFilterKClass.serializer(), apiFilter.value)
        urlParams?.params?.set(pair.first, pair.second)
        @Suppress("UNUSED_VARIABLE")
        val url = (configView.url + urlParams.toString()).asDynamic()

        @Suppress("UNUSED_VARIABLE")
        val stateObj =
            "{apiFilter: toUrl}".asDynamic()
        js("""history.replaceState(stateObj,"createToUpdate",url)""")
    }

    var displayBlock: (() -> Unit)? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var suspendPeriodicUpdate = false
    abstract suspend fun dataUpdate()

    /**
     * Allows to set an initial [apiFilter] value if it can't be obtained from [urlParams]
     */
    open suspend fun initialApiFilter(): FILT? = null

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
     * Builds a new instance of [apiFilter]
     */
    @OptIn(InternalSerializationApi::class)
    open fun newApiFilterInstance(): FILT {
        return try {
            Json.decodeFromString(configViewContainer.apiFilterKClass.serializer(), """{}""")
        } catch (e: Exception) {
            val errMsg = """
                Error creating instance of apiFilter,
                hint: [${configViewContainer.apiFilterKClass.simpleName}]::class must *not* have constructor parameters,
                or need to override the newApiFilterInstance() function
                """.trimIndent()
            e.message
            console.error(errMsg)
            Toast.danger(
                message = errMsg,
                options = ToastOptions(
                    position = ToastPosition.BOTTOMRIGHT,
                    escapeHtml = true,
                    duration = 10000,
                    stopOnFocus = true,
                    newWindow = true
                )
            )
            throw e
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
}
