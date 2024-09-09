package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlinx.browser.window
import org.w3c.dom.Window
import kotlin.reflect.KClass

abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, E : Any, FILT : IApiFilter<MID>, MID : Any>(
    val serviceManager: KVServiceManager<E>,
    val apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    override val commonContainer: CC,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    viewKClass = viewKClass,
    commonContainer = commonContainer,
    baseUrl = baseUrl
) {
    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *, *>>()
    }

    override val label: String get() = commonContainer.labelList
    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelList to url }

    /**
     * builds an url string with optional [IApiFilter] parameter
     */
    fun url(apiFilter: FILT? = null): String {
        return baseUrl + (apiFilter?.let {
            "?" + pairParam(
                key = "apiFilter",
                serializer = commonContainer.apiFilterSerializer,
                obj = apiFilter
            )
        } ?: "")
    }

    fun viewListUrl(apiFilter: FILT = commonContainer.apiFilterInstance()): String {
        val params = mutableListOf<Pair<String, String>>()
        apiFilterParam(apiFilter).let { params.add(it) }
        return urlWithParams(*params.toTypedArray())
    }

    /**
     * Navigates to a specific URL in a new or existing window.
     *
     * @param apiFilter An optional filter to apply to the URL, defaults to an instance of the filter.
     * @param target The target window or frame where the URL should be opened, defaults to "_blank".
     * @return The Window object of the opened URL, or null if the operation fails.
     */
    @Suppress("unused")
    fun navigateTo(
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank"
    ): Window? {
        return window.open(
            url = viewListUrl(apiFilter),
            target = target
        )
    }

    init {
        configViewListMap[this.baseUrl] = this
    }
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, V : ViewList<CC, T, ID, FILT, MID>, E : Any, ID : Any, FILT : IApiFilter<MID>, MID : Any> configViewList(
    viewKClass: KClass<out V>,
    serviceManager: KVServiceManager<E>,
    apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    commonContainer: CC,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, E, FILT, MID> = object : ConfigViewList<CC, T, ID, V, E, FILT, MID>(
    viewKClass = viewKClass,
    serviceManager = serviceManager,
    apiListFun = apiListFun,
    commonContainer = commonContainer,
    baseUrl = baseUrl
) {}
