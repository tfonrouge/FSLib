package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import io.kvision.core.Container
import io.kvision.remote.*
import io.kvision.tabulator.TableType
import io.kvision.tabulator.Tabulator
import io.kvision.tabulator.TabulatorOptions
import io.kvision.utils.Serialization
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import kotlin.js.Promise
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class TabulatorListContainer<T : BaseDoc<U>, E : IDataList, U : Any>(
    serviceManager: KVServiceMgr<E>,
    function: suspend E.(ApiList?) -> ListState<T>,
    private val apiListBlock: (() -> ApiList),
    private val apiListUpdate: (ApiList.() -> Unit)? = null,
    var onResult: ((dynamic) -> Unit)? = null,
    options: TabulatorOptions<T>,
    types: Set<TableType>,
    className: String?,
    kClass: KClass<T>?,
    serializer: KSerializer<T>?,
    module: SerializersModule?,
    private val requestFilter: (suspend RequestInit.() -> Unit)?
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
    private var checksum: String? = null
    private var diffChecksums: Boolean = false
    private var url: String
    private var method: HttpMethod
    private val callAgent: CallAgent

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

    internal fun apiCall() {
        val page: Int? = jsTabulator?.getPage() as? Int
        val size: Int? = jsTabulator?.getPageSize()?.toInt()
        val filters: List<RemoteFilter>? = jsTabulator?.getHeaderFilters()?.map {
            RemoteFilter(field = it.field, type = it.type, value = "${it.value}")
        }
        val sorters: List<RemoteSorter>? = jsTabulator?.getSorters()?.map {
            RemoteSorter(field = it.field, dir = it.dir)
        }
        promise(
            page = page,
            size = size,
            filters = filters,
            sorters = sorters,
        ).then { result: dynamic ->
//            console.warn("RESULT ->", result, "CHECKSUM ->", checksum, "diffChecksums", diffChecksums)
            if (diffChecksums) {
                jsTabulator?.replaceData(result.data, null, null)
            }
        }
    }

    private fun promise(
        page: Int?,
        size: Int?,
        filters: List<RemoteFilter>?,
        sorters: List<RemoteSorter>?,
    ): Promise<dynamic> {
        val contextDataUrl = apiListBlock.invoke().apply {
            tabPage = page
            tabSize = size
            tabFilter = filters
            tabSorter = sorters
            checksum = this@TabulatorListContainer.checksum
        }
        apiListUpdate?.invoke(contextDataUrl)
        val data =
            Serialization.plain.encodeToString(
                JsonRpcRequest(
                    0, url,
                    listOf(
                        contextDataUrl.let { Json.encodeToString(it) }
                    )
                )
            )
        return callAgent.remoteCall(
            url,
            data,
            method = HttpMethod.valueOf(method.name),
            requestFilter = requestFilter
        ).then { r: dynamic ->
//            console.warn("r ->", r, "<-")
            val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
//            console.warn("result ->", result, "<-")
            onResult?.let { it(result) }
            if (result.checksum != undefined) {
                diffChecksums = (result.checksum as? String) != checksum
                checksum = result.checksum as? String
            }
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

    init {
        serviceManager.requireCall(function).let {
            url = it.first
            method = it.second
        }
        callAgent = CallAgent()
        options.ajaxURL = urlPrefix + url.drop(1)
        options.ajaxRequestFunc = { _, _, params ->
            val page: Int? = params.page as? Int
            val size: Int? = params.size as? Int
            val filters = if (params.filter != null) {
                Json.decodeFromString(ListSerializer(RemoteFilter::class.serializer()), JSON.stringify(params.filter))
            } else {
                null
            }
            val sorters = if (params.sort != null) {
                val j = js("[]")
                JSON.stringify(params.sort)
                js(
                    """
                    params.sort.forEach(function(value) {
                        j.push({"field": value["field"], "dir": value["dir"]})
                    })
                """
                )
//                console.warn(">>>", j)
                Json.decodeFromDynamic(ListSerializer(RemoteSorter::class.serializer()), j)
            } else {
                null
            }
            promise(
                page = page,
                size = size,
                filters = filters,
                sorters = sorters,
            )
        }
    }
}

inline fun <reified T : BaseDoc<U>, E : IDataList, U : Any> Container.tabulatorListContainer(
    serviceManager: KVServiceMgr<E>,
    noinline function: suspend E.(ApiList?) -> ListState<T>,
    noinline apiListBlock: (() -> ApiList),
    noinline apiListUpdate: (ApiList.() -> Unit)? = null,
    noinline onResult: ((dynamic) -> Unit)? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    className: String? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    noinline requestFilter: (suspend RequestInit.() -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, U>.() -> Unit)? = null
): TabulatorListContainer<T, E, U> {
    val tabulatorListContainer =
        TabulatorListContainer(
            serviceManager = serviceManager,
            function = function,
            apiListBlock = apiListBlock,
            apiListUpdate = apiListUpdate,
            onResult = onResult,
            options = options,
            types = types,
            className = className,
            kClass = T::class,
            serializer = serializer,
            module = module,
            requestFilter = requestFilter
        )
    init?.invoke(tabulatorListContainer)
    this.add(tabulatorListContainer)
    return tabulatorListContainer
}
