package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, E, FILT>, E : Any, FILT : IApiFilter>(
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
    override val commonContainer: CC,
    viewFunc: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    viewFunc = viewFunc,
    commonContainer = commonContainer,
    baseUrl = baseUrl
) {
    override val baseUrl: String
        get() {
            val result =
                _baseUrl
                    ?: if (commonContainer == undefined) "error: commonContainer undefined" else ("ViewList" + commonContainer.name)
            return result
        }

    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *>>()
    }

    override val label: String get() = commonContainer.labelList
    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelList to url }

    /**
     * builds an url string with optional [IApiFilter] parameter
     */
    fun url(apiFilter: FILT? = null): String {
        return baseUrl + (apiFilter?.let { "?" + pairParam("apiFilter", commonContainer.apiFilterSerializer, apiFilter) }
            ?: "")
    }

    init {
        configViewListMap[this.baseUrl] = this
    }
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, V : ViewList<CC, T, ID, E, FILT>, E : Any, ID : Any, FILT : IApiFilter> configViewList(
    viewFunc: KClass<out V>,
    serviceManager: KVServiceManager<E>,
    function: suspend E.(ApiList<FILT>) -> ListState<T>,
    commonContainer: CC,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, E, FILT> = object : ConfigViewList<CC, T, ID, V, E, FILT>(
    viewFunc = viewFunc,
    serviceManager = serviceManager,
    function = function,
    commonContainer = commonContainer,
    baseUrl = baseUrl
) {}
