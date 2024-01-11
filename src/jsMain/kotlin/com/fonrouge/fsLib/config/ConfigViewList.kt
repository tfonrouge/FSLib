package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<CV : ICommonList<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CV, T, ID, E, FILT>, E : Any, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
    override val commonView: CV,
    viewFunc: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CV, V, FILT>(
    viewFunc = viewFunc,
    commonView = commonView,
    baseUrl = baseUrl
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *>>()
    }

    /**
     * builds an url string with optional [IApiFilter] parameter
     */
    fun url(apiFilter: FILT? = null): String {
        return baseUrl + (apiFilter?.let { "?" + pairParam("apiFilter", commonView.apiFilterSerializer, apiFilter) }
            ?: "")
    }

    init {
        console.warn("ConfigViewList REGISTERING WITH", this.baseUrl)
        configViewListMap[this.baseUrl] = this
    }
}

@Suppress("unused")
inline fun <CV : ICommonList<T, ID, FILT>, reified T : BaseDoc<ID>, V : ViewList<CV, T, ID, E, FILT>, E : Any, ID : Any, FILT : IApiFilter> configViewList(
    itemKClass: KClass<T> = T::class,
    viewFunc: KClass<out V>,
    serviceManager: KVServiceManager<E>,
    noinline function: suspend E.(ApiList<FILT>) -> ListState<T>,
    commonView: CV,
    baseUrl: String? = null,
): ConfigViewList<CV, T, ID, V, E, FILT> = object : ConfigViewList<CV, T, ID, V, E, FILT>(
    itemKClass = itemKClass,
    viewFunc = viewFunc,
    serviceManager = serviceManager,
    function = function,
    commonView = commonView,
    baseUrl = baseUrl
) {}
