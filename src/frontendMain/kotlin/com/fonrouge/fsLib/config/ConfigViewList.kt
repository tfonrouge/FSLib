package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseDoc<ID>, V : ViewList<T, E, ID, FILT>, E : IDataList, ID : Any, FILT : ApiFilter>(
    itemKClass: KClass<T>,
    idKClass: KClass<ID>? = null,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ApiList<FILT>) -> ListState<T>,
) : ConfigViewContainer<T, V, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *>>()
    }

    init {
        configViewListMap[baseUrl] = this
    }
}

@Suppress("unused")
fun <T : BaseDoc<ID>, V : ViewList<T, E, ID, FILT>, E : IDataList, ID : Any, FILT : ApiFilter> configViewList(
    itemKClass: KClass<T>,
    idKClass: KClass<ID>? = null,
    apiFilterKClass: KClass<FILT>,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    serviceManager: KVServiceManager<E>,
    function: suspend E.(ApiList<FILT>) -> ListState<T>,
): ConfigViewList<T, V, E, ID, FILT> = object : ConfigViewList<T, V, E, ID, FILT>(
    itemKClass = itemKClass,
    idKClass = idKClass,
    apiFilterKClass = apiFilterKClass,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
    serviceManager = serviceManager,
    function = function,
) {}
