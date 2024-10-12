package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.remote.*
import io.kvision.tabulator.TableType
import io.kvision.tabulator.Tabulator
import io.kvision.tabulator.TabulatorOptions
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
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
class TabulatorListContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any>(
    val viewList: ViewList<out ICommonContainer<T, ID, FILT>, T, ID, FILT, MID>,
    private val apiListBlock: (() -> ApiList<FILT>),
    private val apiListSerialize: (ApiList<FILT>) -> String?,
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
    private var contentHashCode: Int? = null
    private var diffContentHashCode: Boolean = false
    private var url: String = ""
    private var method: HttpMethod = HttpMethod.GET
    private val callAgent: CallAgent

    override val jsonHelper = if (serializer != null) Json(
        from = (RemoteSerialization.customConfiguration ?: Serialization.customConfiguration
        ?: Json {
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
        val page: Int = jsTabulator?.getPage() as? Int ?: 1
        val size: Int = jsTabulator?.getPageSize()?.toInt() ?: 50
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
//            console.warn("RESULT ->", result, "CONTENT_HASHCODE ->", contentHashCode, "diffContentHashCode", diffContentHashCode)
            if (diffContentHashCode) {
                replaceData(
                    Json.decodeFromDynamic(
                        ListSerializer(viewList.configView.commonContainer.itemSerializer),
                        result.data
                    ).toTypedArray()
                )
//                jsTabulator?.replaceData(result.data, null, null)
            }
        }
    }

    private fun promise(
        page: Int,
        size: Int,
        filters: List<RemoteFilter>?,
        sorters: List<RemoteSorter>?,
    ): Promise<dynamic> {
        val apiList = apiListBlock.invoke().apply {
            tabPage = page
            tabSize = size
            tabFilter = filters
            tabSorter = sorters
            contentHashCode = this@TabulatorListContainer.contentHashCode
        }
        val data =
            Serialization.plain.encodeToString(
                JsonRpcRequest(
                    id = 0,
                    method = url,
                    params = listOf(
                        apiListSerialize.invoke(apiList)
                    )
                )
            )
        return callAgent.remoteCall(
            url,
            data,
            method = HttpMethod.valueOf(method.name),
            requestFilter = requestFilter
        ).then { r: dynamic ->
            if (r.result != undefined) {
                val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
                if (result.data == undefined) {
                    result.data = "[]"
                }
                result.contentHashCode = (result.data as String).hashCode()
                if (result.contentHashCode != undefined) {
                    diffContentHashCode = (result.contentHashCode as? Int) != contentHashCode
                    contentHashCode = result.contentHashCode as? Int
                }
                result.data = js("eval(result.data)")

//                console.warn("result received", result)
//                val list = Json.decodeFromDynamic(
//                    ListSerializer(viewList.configView.commonContainer.itemSerializer),
//                    result.data
//                )
//                console.warn("decoded list", list)

                viewList.onReceivingData(result.data)
                result
            } else {
                console.error("Server response error:", r)
                if (r.error != undefined && r.exceptionType != undefined) {
                    Toast.danger(
                        message = "Server response error -> ${r.error}, exceptionType -> ${r.exceptionType}",
                        options = ToastOptions(avatar = "")
                    )
                }
                null
            }
        }
    }

    init {
        viewList.configView.serviceManager.requireCall(viewList.configView.apiListFun).let {
            url = it.first
            method = it.second
        }
        callAgent = CallAgent()
        options.ajaxURL = urlPrefix + url.drop(1)
        options.ajaxRequestFunc = { _, _, params ->
            val page: Int = params?.asDynamic()?.page as? Int ?: 1
            val size: Int = params?.asDynamic()?.size as? Int ?: 50
            val filters = if (params?.asDynamic()?.filter != null) {
                Json.decodeFromString(
                    ListSerializer(RemoteFilter::class.serializer()),
                    JSON.stringify(params.asDynamic()?.filter)
                )
            } else {
                null
            }
            val sorters = if (params?.asDynamic()?.sort != null) {
                val j = js("[]")
                JSON.stringify(params.asDynamic()?.sort)
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

inline fun <reified T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any> Container.tabulatorListContainer(
    viewList: ViewList<out ICommonContainer<T, ID, FILT>, T, ID, FILT, MID>,
    noinline apiListBlock: (() -> ApiList<FILT>),
    noinline apiListSerialize: (ApiList<FILT>) -> String?,
    options: TabulatorOptions<T>,
    types: Set<TableType> = setOf(),
    className: String? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    noinline requestFilter: (suspend RequestInit.() -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, ID, FILT, MID>.() -> Unit)? = null
): TabulatorListContainer<T, ID, FILT, MID> {
    val tabulatorListContainer: TabulatorListContainer<T, ID, FILT, MID> =
        TabulatorListContainer(
            viewList = viewList,
            apiListBlock = apiListBlock,
            apiListSerialize = apiListSerialize,
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
