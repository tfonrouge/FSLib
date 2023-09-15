package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, E, FILT>, E : IDataList, FILT : ApiFilter>(
    itemKClass: KClass<T>,
    idKClass: KClass<ID>,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
) : ConfigViewContainer<T, V, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *>>()
    }

    init {
        configViewListMap[baseUrl] = this
    }
}

@Suppress("unused")
inline fun <reified T : BaseDoc<ID>, reified V : ViewList<T, ID, E, FILT>, E : IDataList, reified ID : Any, reified FILT : ApiFilter> configViewList(
    itemKClass: KClass<T>,
    idKClass: KClass<ID> = ID::class,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    requireCredentials: Boolean = true,
    serviceManager: KVServiceManager<E>,
    noinline function: suspend E.(ApiList<FILT>) -> ListState<T>,
): ConfigViewList<T, ID, V, E, FILT> = object : ConfigViewList<T, ID, V, E, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    requireCredentials = requireCredentials,
    serviceManager = serviceManager,
    function = function,
) {}
