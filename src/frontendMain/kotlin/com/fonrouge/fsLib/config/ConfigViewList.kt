package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseDoc<U>, V : ViewList<T, E, U, F>, E : IDataList, U : Any, F : IApiFilter>(
    val itemKClass: KClass<T>,
    idKClass: KClass<U>? = null,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList, F?) -> ListState<T>,
    val apiFilterKClass: KClass<F>? = null,
) : ConfigViewContainer<T, V, U>(
    idKClass = idKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *>>()
    }

    /**
     * helper to build an api filter parameter in the url string
     */
    inline fun <reified T : IApiFilter> urlApiFilter(obj: T): String =
        pushUrlParam(pairParam("apiFilter", obj))

    init {
        configViewListMap[baseUrl] = this
    }
}

@Suppress("unused")
fun <T : BaseDoc<U>, V : ViewList<T, E, U, F>, E : IDataList, U : Any, F : IApiFilter> configViewList(
    itemKClass: KClass<T>,
    idKClass: KClass<U>? = null,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    serviceManager: KVServiceManager<E>,
    function: suspend E.(ApiList, F?) -> ListState<T>,
): ConfigViewList<T, V, E, U, F> = object : ConfigViewList<T, V, E, U, F>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    serviceManager = serviceManager,
    function = function,
) {}
