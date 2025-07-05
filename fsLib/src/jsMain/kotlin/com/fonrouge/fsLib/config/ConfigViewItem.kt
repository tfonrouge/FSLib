package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.apiItemQueryReadCall
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.view.KVWebManager.configViewItemMap
import com.fonrouge.fsLib.view.ViewItem
import dev.kilua.rpc.RpcServiceManager
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import web.prompts.alert
import kotlin.js.Promise
import kotlin.reflect.KClass

/**
 * Abstract class providing configuration and utility functions for managing view items in a web-based application.
 *
 * @param CC Type of the container responsible for handling data and API interaction. Must extend ICommonContainer.
 * @param T Type of the data model for the view item. Must extend BaseDoc.
 * @param ID Type of the unique identifier for the data model.
 * @param V Type of the view item associated with the configuration class.
 * @param FILT Type of the API filter for querying data. Must extend IApiFilter.
 * @param AIS Type for any additional custom handler logic, typically a service-related class.
 * @property commonContainer The shared container managing API and data interactions.
 * @property apiItemFun A suspend function allowing interaction with custom API logic.
 * @property viewKClass Reference to the Kotlin class associated with the view item.
 * @property baseUrl Configurable base endpoint URL. Defaults to the simple name of the associated view class.
 */
abstract class ConfigViewItem<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any>(
    commonContainer: CC,
    val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null,
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl,
) {
    companion object {
        private var dataItemServiceManager: RpcServiceManager<*>? = null
        var serviceManager: RpcServiceManager<*>
            get() = dataItemServiceManager
                ?: throw IllegalStateException("serviceManager is null. Please set ConfigViewItem.serviceManager value before instantiating any ConfigViewItem.".also {
                    alert(
                        it
                    )
                })
            set(value) {
                dataItemServiceManager = value
            }
    }

    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    override val label: String get() = commonContainer.labelItem

    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelItem to url }

    /**
     * Executes a query to fetch a specific item using its identifier and processes the resulting state.
     *
     * @param R The return type of the transformation function applied to the resulting [ItemState].
     * @param id The identifier of the item to be queried.
     * @param apiFilter An optional filter of type `FILT` to refine the API query; defaults to a new instance of `FILT`.
     * @param transform A function that processes the resulting [ItemState] and transforms it into the desired type [R].
     * @return A `Promise` wrapping the result of the `transform` function applied to the queried item's [ItemState].
     */
    @Suppress("unused")
    fun <R> apiQueryReadCall(
        id: ID,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        transform: (ItemState<T>) -> R,
    ): Promise<R> {
        return commonContainer.apiItemQueryReadCall(
            apiItemFun = apiItemFun,
            id = id,
            apiFilter = apiFilter,
            transform = transform
        )
    }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = commonContainer.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = commonContainer.labelItem to urlUpdate(id)

    /**
     * Navigates to the URL associated with the given API item in a specified target window or tab.
     *
     * @param apiItem the API item from which the URL will be resolved and navigated to
     * @param target the target window or tab where the URL should be opened; default is "_blank"
     * @return the opened [Window] instance if the URL is successfully resolved and opened, or `null` if the URL cannot be resolved
     */
    @Suppress("unused")
    fun navigateTo(
        apiItem: ApiItem<T, ID, FILT>, target: String = "_blank",
    ): Window? = viewItemUrl(apiItem)?.let { url ->
        window.open(
            url = url,
            target = target
        )
    }

    /**
     * Navigates to the URL associated with the given item identifier in a specified target window or tab.
     *
     * @param id The identifier of the item to retrieve and navigate to.
     * @param apiFilter An optional filter of type `FILT` to customize the API query; defaults to a new instance of `FILT`.
     * @param target The target window or tab where the URL should be opened; default is "_blank".
     * @return The opened [Window] instance if the URL is successfully resolved and opened, or `null` if the URL cannot be resolved.
     */
    @Suppress("unused")
    fun navigateTo(
        id: ID,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank",
    ): Window? = navigateTo(
        apiItem = commonContainer.apiItemQueryRead(
            id = id,
            apiFilter = apiFilter
        ),
        target = target
    )

    @Suppress("unused")
    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toEncodedUrlString()
        }

    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Read.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Delete.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(commonContainer.idSerializer, id),
                "action" to CrudTask.Update.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun viewItemUrl(apiItem: ApiItem<T, ID, FILT>): String? {
        val url: String? = when (apiItem) {
            is ApiItem.Upsert.Create.Query -> listOf(
                "action" to CrudTask.Create.name,
            ) + (apiItem.id?.let {
                listOf(
                    "id" to Json.encodeToString(commonContainer.idSerializer, it)
                )
            } ?: emptyList())

            is ApiItem.Read -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Upsert.Update.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Delete.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            else -> null
        }?.let { params: List<Pair<String, String>> ->
            val urlParams = UrlParams(*params.toTypedArray())
            urlParams.pushParam(
                "apiFilter" to Json.encodeToString(
                    commonContainer.apiFilterSerializer,
                    apiItem.apiFilter
                )
            )
            url + urlParams.toEncodedUrlString()
        }
        return url
    }

    init {
        configViewItemMap[this.baseUrl] = this
    }
}

/**
 * Configures and creates a `ConfigViewItem` instance for managing view items in a specific context.
 *
 * @param viewKClass The KClass instance representing the view item type.
 * @param commonContainer An instance of `ICommonContainer` that manages the API items of a specific type.
 * @param apiItemFun A suspend function to process API items and determine their state.
 * @param baseUrl An optional base URL for the configuration; defaults to null.
 * @return A configured `ConfigViewItem` instance for the specified parameters.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any> configViewItem(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    baseUrl: String? = null,
): ConfigViewItem<CC, T, ID, V, FILT, AIS> = object : ConfigViewItem<CC, T, ID, V, FILT, AIS>(
    commonContainer = commonContainer,
    apiItemFun = apiItemFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
