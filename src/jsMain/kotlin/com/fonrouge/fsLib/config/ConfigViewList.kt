package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, E, FILT>, E : IDataList, FILT : IApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>,
    apiFilterKClass: KClass<FILT>,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
    override val commonView: ICommonViewList<T, ID, FILT>,
) : ConfigViewContainer<V, FILT>(
    apiFilterKClass = apiFilterKClass,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
    commonView = commonView,
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *>>()
    }

    init {
        configViewListMap[baseUrl] = this
    }
}

@Suppress("unused")
inline fun <reified T : BaseDoc<ID>, V : ViewList<T, ID, E, FILT>, E : IDataList, reified ID : Any, reified FILT : IApiFilter> configViewList(
    itemKClass: KClass<T> = T::class,
    idKClass: KClass<ID> = ID::class,
    apiFilterKClass: KClass<FILT> = FILT::class,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean = true,
    serviceManager: KVServiceManager<E>,
    noinline function: suspend E.(ApiList<FILT>) -> ListState<T>,
    commonView: ICommonViewList<T, ID, FILT>,
): ConfigViewList<T, ID, V, E, FILT> = object : ConfigViewList<T, ID, V, E, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
    serviceManager = serviceManager,
    function = function,
    commonView = commonView,
) {}
