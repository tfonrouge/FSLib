package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.ListContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseDoc<U>, V : ViewList<T, E, U>, E : IDataList, U : Any>(
    val itemKClass: KClass<T>,
    idKClass: KClass<U>? = null,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String = viewFunc.simpleName!!,
    val serviceManager: KVServiceManager<E>,
    val function: suspend E.(ContextDataUrl?) -> ListContainer<T>,
) : ConfigViewContainer<T, V, U>(
    idKClass = idKClass,
    name = itemKClass.simpleName!!,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl
) {
    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *>>()
    }

    init {
        configViewListMap[baseUrl] = this
    }
}
