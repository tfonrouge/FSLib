package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

abstract class ConfigViewList<T : BaseModel<U>, V : ViewList<T, E, U>, E : IDataList, U>(
    private val klass: KClass<T>,
    label: String,
    viewFunc: ((UrlParams?) -> V),
    val serverManager: KVServiceManager<E>,
    val function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, String?) -> RemoteData<T>,
    restUrlParams: UrlParams? = null,
    lookupParam: JsonObject? = null,
) : ConfigViewContainer<T, V>(
    name = klass.simpleName!!,
    label = label,
    restUrlParams = restUrlParams,
    lookupParam = lookupParam,
    typeView = TypeView.List,
    viewFunc = viewFunc,
) {

    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *>>()
    }

    init {
        configViewListMap[name] = this
    }
}
