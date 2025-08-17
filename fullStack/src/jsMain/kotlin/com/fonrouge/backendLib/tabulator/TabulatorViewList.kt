package com.fonrouge.backendLib.tabulator

import com.fonrouge.backendLib.config.ConfigViewList
import com.fonrouge.backendLib.view.ViewList
import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.State
import dev.kilua.rpc.*
import io.kvision.core.Container
import io.kvision.core.KVScope
import io.kvision.tabulator.PaginationMode
import io.kvision.tabulator.TableType
import io.kvision.tabulator.Tabulator
import io.kvision.tabulator.TabulatorOptions
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.utils.Serialization
import kotlinx.browser.window
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.serializer
import org.w3c.dom.get
import kotlin.js.Promise
import kotlin.reflect.KClass

/**
 * A class encapsulating a tabulated view list with added functionality for server-side data interaction.
 *
 * @param T The type of data objects managed in the table. Must extend [BaseDoc].
 * @param ID The type representing the unique identifier for each data object.
 * @param FILT The filter type used for the API calls. Must extend [IApiFilter].
 * @param MID The type representing the filter ID.
 * @param viewList An instance of [com.fonrouge.backendLib.view.ViewList] containing view-related configurations and container data.
 * @param apiListBlock A lambda returning an instance of [ApiList] for filter and pagination setup.
 * @param apiListSerialize A function to serialize [ApiList] into a `String` for API interaction.
 * @param options Table-specific options to configure the tabulator instance.
 * @param types A set of [TableType] to define the functionalities and behaviors of the table.
 * @param className Optionally specifies a CSS class name for styling the tabulator.
 * @param kClass Optionally specifies a Kotlin class type for serialization and reflection purposes.
 * @param serializer A serializer instance for serializing and deserializing the data type [T].
 * @param module A [SerializersModule] to support custom and polymorphic serialization.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class TabulatorViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any>(
    val viewList: ViewList<CC, T, ID, FILT, MID>,
    private val apiListBlock: (() -> ApiList<FILT>),
    private val apiListSerialize: (ApiList<FILT>) -> String?,
    tabulatorOptions: TabulatorOptions<T>,
    types: Set<TableType>,
    className: String?,
    kClass: KClass<T>?,
    serializer: KSerializer<T>?,
    module: SerializersModule?,
) : Tabulator<T>(
    data = null,
    dataUpdateOnEdit = false,
    // TODO: Fix error when not using the following parameters when decoding api result in [TabulatorViewList.promise]
    // which returns an object ("{}") instead of a list ("[]")
    options = tabulatorOptions.copy(
        pagination = true,
        paginationMode = PaginationMode.REMOTE
    ),
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
        from = (RpcSerialization.customConfiguration ?: Serialization.customConfiguration
        ?: Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    ) {
        serializersModule = SerializersModule {
            include(RpcSerialization.plain.serializersModule)
            module?.let { this.include(it) }
        }.overwriteWith(serializersModule)
    } else null

    private val kvUrlPrefix = window["kv_remote_url_prefix"]
    private val urlPrefix: String = if (kvUrlPrefix != undefined) "$kvUrlPrefix/" else ""

    /**
     * A callback that is triggered when a page is successfully loaded.
     *
     * The variable allows the assignment of a lambda function or a method reference,
     * which will be invoked upon the completion of a page load event.
     * The single `Int` parameter passed to the callback typically represents
     * the page number or an identifier related to the loaded page.
     *
     * The callback is nullable, so it can remain `null` if no operation
     * should be executed after a page is loaded.
     */
    var onPageLoaded: ((Int) -> Unit)? = null

    /**
     * A callback function that is triggered when a row in the list is selected.
     *
     * The lambda receives the selected item of type [T] or `null` if no row is selected.
     * This property can be used to handle specific actions or behaviors when a row is selected
     * in the user interface.
     *
     * Setting this property to `null` will disable the callback.
     */
    var onRowSelected: ((T?) -> Unit)? = null

    /**
     * Converts a dynamic data object into a Kotlin List of type [T].
     *
     * @param data The dynamic data object to be converted into a Kotlin List.
     * The data is expected to be in a JSON-compatible format.
     *
     * @return A Kotlin List of type [T] generated from the input data.
     * If an error occurs during the conversion, an empty list is returned.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun toKotlinList(data: dynamic): List<T> =
        try {
            Json.decodeFromDynamic(
                ListSerializer(viewList.configView.commonContainer.itemSerializer),
                data
            )
        } catch (_: Exception) {
//            console.error(e)
            emptyList()
        }

    /**
     * Executes an API call to fetch and process paginated data with filters and sorters applied.
     *
     * The method retrieves the current page number, page size, header filters, and sorters
     * from the associated jsTabulator instance. These parameters are then used to invoke a
     * promise that interacts with the server to fetch data. Upon receiving the data,
     * the method determines whether the content has changed by comparing hash codes.
     *
     * If the data differs from the current content, it replaces the existing content by
     * transforming the retrieved data into a Kotlin List using the `toKotlinList` method.
     *
     * The method relies on the following components:
     * - `jsTabulator`: An external tabulator instance to fetch page, filters, and sorters data.
     * - `promise`: A private function that handles the server call with the specified parameters
     *   including pagination, filters, and sorters.
     * - `diffContentHashCode`: A flag indicating whether the content has changed based on hash codes.
     * - `replaceData()`: Called to update the data if changes are detected.
     *
     * This function is designed to handle dynamic data efficiently and integrate with the
     * server-side logic to ensure the data displayed reflects the latest state based on the
     * applied filters and sorting.
     */
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
                if (result.data != undefined) {
                    replaceData(toKotlinList(result.data).toTypedArray())
                }
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
            contentHashCode = this@TabulatorViewList.contentHashCode
        }
        return KVScope.async {
            try {
                val jsonObj = this@TabulatorViewList.callAgent.jsonRpcCall(
                    this@TabulatorViewList.url,
                    listOf(this@TabulatorViewList.apiListSerialize.invoke(apiList)),
                    this@TabulatorViewList.method
                ).let {
//                    console.warn("${viewList.configView.viewKClass.simpleName} jsonRpcCall result", it)
                    JSON.parse<dynamic>(it)
                }
//                console.warn("${viewList.configView.viewKClass.simpleName} jsonObj", jsonObj)
                if (jsonObj.data == undefined) {
                    jsonObj.data = js("[]")
                }
                jsonObj.contentHashCode = JSON.stringify(jsonObj.data).hashCode()
                if (jsonObj.contentHashCode != undefined) {
                    this@TabulatorViewList.diffContentHashCode =
                        (jsonObj.contentHashCode as? Int) != this@TabulatorViewList.contentHashCode
                    this@TabulatorViewList.contentHashCode = jsonObj.contentHashCode as? Int
                }
                ((jsonObj.state as? String) == State.Error.name).also { errorState ->
                    if (this@TabulatorViewList.viewList.errorStateObs.value != errorState) {
                        this@TabulatorViewList.viewList.errorStateObs.value = errorState
                    }
                    this@TabulatorViewList.viewList.errorMessage = if (errorState) jsonObj.msgError as? String else null
                    if (errorState) {
                        Toast.danger(
                            message = this@TabulatorViewList.viewList.errorMessage ?: "Unknown error",
                            options = ToastOptions(avatar = "")
                        )
                    }
                }
                this@TabulatorViewList.viewList.onReceivingData(jsonObj.data)
                jsonObj
            } catch (e: Exception) {
                console.error("Server call response error:", e.message)
                Toast.danger(
                    message = "Server call response error: ${e.message}",
                    options = ToastOptions(avatar = "")
                )
                js("{}")
            }
        }.asPromise()
    }

    init {
        ConfigViewList.serviceManager.requireCall(viewList.configView.apiListFun).let {
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

/**
 * Creates and adds a Tabulator view list to the specified container.
 *
 * @param T The type of the data objects used in the table, extending BaseDoc.
 * @param ID The type of the unique identifier for the data objects, extending Any.
 * @param FILT The type of the API filter used for filtering data, extending IApiFilter.
 * @param MID The type of the master item identifier in the filter, extending Any.
 * @param viewList The view list object that provides the container and its data-related logic.
 * @param apiListBlock A lambda function that generates the API list used for querying the data.
 * @param apiListSerialize A lambda function that serializes the API list into a string format for transmission.
 * @param tabulatorOptions The configuration options for the Tabulator table.
 * @param types A set of table types that define specific table features or behaviors. Defaults to an empty set.
 * @param className An optional class name applied to the Tabulator container for styling or other purposes. Defaults to null.
 * @param serializer An optional serializer used for serializing/deserializing instances of type T. Defaults to null.
 * @param module An optional serializers module used to resolve any custom serialization logic. Defaults to null.
 * @param init An optional initialization block applied to the TabulatorViewList instance. Defaults to null.
 * @return An instance of TabulatorViewList configured with the provided parameters, added to the container.
 */
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any> Container.tabulatorViewList(
    viewList: ViewList<CC, T, ID, FILT, MID>,
    apiListBlock: (() -> ApiList<FILT>),
    apiListSerialize: (ApiList<FILT>) -> String?,
    tabulatorOptions: TabulatorOptions<T> = viewList.defaultTabulatorOptions(),
    types: Set<TableType> = setOf(),
    className: String? = null,
    serializer: KSerializer<T>? = null,
    module: SerializersModule? = null,
    init: (TabulatorViewList<CC, T, ID, FILT, MID>.() -> Unit)? = null,
): TabulatorViewList<CC, T, ID, FILT, MID> {
    val tabulatorViewList: TabulatorViewList<CC, T, ID, FILT, MID> =
        TabulatorViewList(
            viewList = viewList,
            apiListBlock = apiListBlock,
            apiListSerialize = apiListSerialize,
            tabulatorOptions = tabulatorOptions,
            types = types,
            className = className,
            kClass = viewList.configView.commonContainer.itemKClass,
            serializer = serializer,
            module = module,
        )
    init?.invoke(tabulatorViewList)
    this.add(tabulatorViewList)
    return tabulatorViewList
}
