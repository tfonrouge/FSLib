package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.model.ListContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.remote.*
import io.kvision.tabulator.*
import io.kvision.utils.Serialization
import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import kotlin.js.JSON
import kotlin.reflect.KClass

class TabulatorListContainer<T : BaseModel<*>, E : Any>(
    serviceManager: KVServiceMgr<E>,
    function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, ContextDataUrl?) -> ListContainer<T>,
    stateFunction: (() -> String)?,
    options: TabulatorOptions<T>,
    types: Set<TableType>,
    className: String?,
    kClass: KClass<T>?,
    serializer: KSerializer<T>?,
    module: SerializersModule?,
    requestFilter: (suspend RequestInit.() -> Unit)?
) : Tabulator<T>(
    data = null,
    dataUpdateOnEdit = false,
    options = options,
    types = types,
    className = className,
    kClass = kClass,
    serializer = serializer,
    module = module
) {
    override val jsonHelper = if (serializer != null) Json(
        from = (RemoteSerialization.customConfiguration ?: Serialization.customConfiguration ?: Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    ) {
        serializersModule = SerializersModule {
            include(RemoteSerialization.plain.serializersModule)
            module?.let { this.include(it) }
        }.overwriteWith(serializersModule)
    } else null

    private val kvUrlPrefix = window["kv_remote_url_prefix"]
    private val urlPrefix: String = if (kvUrlPrefix != undefined) "$kvUrlPrefix/" else ""

    init {
        val (url, method) = serviceManager.requireCall(function)

        val callAgent = CallAgent()

        options.ajaxURL = urlPrefix + url.drop(1)
        options.ajaxRequestFunc = { _, _, params ->
            @Suppress("UnsafeCastFromDynamic")
            val page = if (params.page != null) "" + params.page else null

            @Suppress("UnsafeCastFromDynamic")
            val size = if (params.size != null) "" + params.size else null

            @Suppress("UnsafeCastFromDynamic")
            val filters = if (params.filter != null) {
                JSON.stringify(params.filter)
            } else {
                null
            }

            @Suppress("UnsafeCastFromDynamic")
            val sorters = if (params.sort != null) {
                JSON.stringify(params.sort)
            } else {
                null
            }
            val state = stateFunction?.invoke()?.let { JSON.stringify(it) }

            @Suppress("UnsafeCastFromDynamic")
            val data =
                Serialization.plain.encodeToString(JsonRpcRequest(0, url, listOf(page, size, filters, sorters, state)))
            callAgent.remoteCall(url, data, method = HttpMethod.valueOf(method.name), requestFilter = requestFilter)
                .then { r: dynamic ->
                    val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                    @Suppress("UnsafeCastFromDynamic")
                    if (page != null) {
                        if (result.data == undefined) {
                            result.data = js("[]")
                        }
                        result
                    } else if (result.data == undefined) {
                        js("[]")
                    } else {
                        result.data
                    }
                }
        }
    }
}

inline fun <reified T : BaseModel<*>, E : Any> Container.tabulatorListContainer(
    serviceManager: KVServiceMgr<E>,
    noinline function: suspend E.(Int?, Int?, List<RemoteFilter>?, List<RemoteSorter>?, ContextDataUrl?) -> ListContainer<T>,
    noinline stateFunction: (() -> String)? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    className: String? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    noinline requestFilter: (suspend RequestInit.() -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E>.() -> Unit)? = null
): TabulatorListContainer<T, E> {
    val tabulatorListContainer =
        TabulatorListContainer(
            serviceManager,
            function,
            stateFunction,
            options,
            types,
            className,
            T::class,
            serializer,
            module,
            requestFilter
        )
    init?.invoke(tabulatorListContainer)
    this.add(tabulatorListContainer)
    return tabulatorListContainer
}
