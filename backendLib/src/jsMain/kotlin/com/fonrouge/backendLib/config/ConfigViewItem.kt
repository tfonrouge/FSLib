package com.fonrouge.backendLib.config

import com.fonrouge.backendLib.apiItemQueryReadCall
import com.fonrouge.backendLib.tabulator.TabulatorMenuItem
import com.fonrouge.backendLib.view.KVWebManager.configViewItemMap
import com.fonrouge.backendLib.view.ViewItem
import com.fonrouge.fsLib.api.ApiItem
import com.fonrouge.fsLib.api.CrudTask
import com.fonrouge.fsLib.api.IApiFilter
import com.fonrouge.fsLib.api.IApiItem
import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.model.BaseDoc
import com.fonrouge.fsLib.state.ItemState
import dev.kilua.rpc.RpcServiceManager
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import web.prompts.alert
import kotlin.js.Promise
import kotlin.reflect.KClass

/**
 * Represents an abstract configuration view item that integrates with a common container, enabling interaction
 * with items and views in a structured manner. It provides utilities for managing URLs, navigating views, and
 * querying API-related operations.
 *
 * @param CC The type of the common container that manages item interactions.
 * @param T The type of the item or document being processed.
 * @param ID The type of the identifier associated with the item or document.
 * @param V The type of the view item associated with the configuration.
 * @param FILT The type of API filter used for queries.
 * @param AIS The type of the asynchronous item service.
 * @param commonContainer The common container instance responsible for managing items and their corresponding views.
 * @param apiItemFun A suspending function for fetching item states using an API query.
 * @param viewKClass The class of the view item associated with this configuration.
 * @param contextMenuItems An optional function providing context menu items for a specific item.
 * @param baseUrl The base URL for the configuration; defaults to the name of the view class if not specified.
 */
abstract class ConfigViewItem<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any>(
    commonContainer: CC,
    val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    viewKClass: KClass<out V>,
    val contextMenuItems: ((T) -> List<TabulatorMenuItem>)? = null,
    baseUrl: String? = null,
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl,
) {
    companion object {
        private var dataItemServiceManager: RpcServiceManager<*>? = null

        /**
         * Manages the RPC service manager instance associated with the `ConfigViewItem`.
         *
         * This variable handles the retrieval and assignment of a `RpcServiceManager` instance.
         * Attempting to access the property without setting a value will throw an `IllegalStateException`
         * and trigger an alert with the associated error message.
         *
         * Throws:
         * - `IllegalStateException` if accessed before being initialized.
         */
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

        /**
         * Defines a default context menu provider for a `ConfigViewItem`. This variable is a higher-order function
         * that generates a list of `TabulatorMenuItem` for a given context represented by a `BaseDoc`.
         *
         * The function takes in a `BaseDoc` instance and applies it to the `ConfigViewItem` context
         * to produce an optional list of tabular menu items. This list can define actions, separators,
         * or hierarchies displayed as part of the context menu.
         *
         * It is nullable, meaning the default context menu may not always be configured.
         */
        var contextMenuDefault: (ConfigViewItem<*, *, *, *, *, *>.() -> List<TabulatorMenuItem>?)? =
            null
    }

    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    var item: T? = null

    override val label: String get() = commonContainer.labelItem

    @Suppress("unused")
    val labelId get() = commonContainer.labelItemId(item)

    @Suppress("unused")
    val labelItemId get() = commonContainer.labelItemId(item)

    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelItem to url }

    /**
     * Provides the serialized identifier of the current item using the provided ID serializer.
     * The serialization process is performed only if the `item` property is not null.
     * Returns the JSON-encoded string of the item's identifier.
     */
    @Suppress("unused")
    val serializedId get() = item?.let { Json.encodeToString(commonContainer.idSerializer, it._id) }

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
     * Navigates to the creation query URL for a specific item using an optional identifier and a filter.
     *
     * @param id The optional identifier of the item to include in the creation query; defaults to `null`.
     * @param apiFilter An optional filter of type `FILT` to customize the creation query; defaults to a new instance of `FILT`.
     * @param target The target window or tab where the creation query URL should be opened; default is "_blank".
     * @return The opened [Window] instance if the URL is successfully resolved and opened, or `null` if the URL cannot be resolved.
     */
    @Suppress("unused")
    fun navigateToQueryCreate(
        id: ID? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank",
    ): Window? = navigateTo(
        apiItem = commonContainer.apiItemQueryCreate(
            id = id,
            apiFilter = apiFilter
        ),
        target = target
    )

    /**
     * Navigates to the URL associated with the given item identifier in a specified target window or tab.
     *
     * @param id The identifier of the item to retrieve and navigate to.
     * @param apiFilter An optional filter of type `FILT` to customize the API query; defaults to a new instance of `FILT`.
     * @param target The target window or tab where the URL should be opened; default is "_blank".
     * @return The opened [Window] instance if the URL is successfully resolved and opened, or `null` if the URL cannot be resolved.
     */
    @Suppress("unused")
    fun navigateToQueryRead(
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

    /**
     * Navigates to the update query URL for a specific item using its identifier and an optional filter.
     *
     * @param id The identifier of the item for which the update query URL will be generated.
     * @param apiFilter An optional filter of type `FILT` to customize the update query; defaults to a new instance of `FILT`.
     * @param target The target window or tab where the update query URL should be opened; default is "_blank".
     * @return The opened [Window] instance if the URL is successfully resolved and opened, or `null` if the URL cannot be resolved.
     */
    @Suppress("unused")
    fun navigateToQueryUpdate(
        id: ID,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank",
    ): Window? = navigateTo(
        apiItem = commonContainer.apiItemQueryUpdate(
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
            is ApiItem.Query.Create -> listOf(
                "action" to CrudTask.Create.name,
            ) + (apiItem.id?.let {
                listOf(
                    "id" to Json.encodeToString(commonContainer.idSerializer, it)
                )
            } ?: emptyList())

            is ApiItem.Query.Read -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Query.Update -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Query.Delete -> listOf(
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
 * Configures a custom view item by combining the provided parameters into a configuration object.
 *
 * @param viewKClass The Kotlin class of the view item to be configured. This specifies the type of the view.
 * @param commonContainer The container managing the API items and facilitating interaction with the provided items.
 * @param apiItemFun A suspending function that processes API items and returns their corresponding item states.
 * @param contextMenuItems An optional lambda function to generate a list of context menu items for a given item of type T.
 * @param baseUrl An optional base URL to be used in the configuration of the view item.
 * @return A configuration object of the type ConfigViewItem for the provided generic parameters.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any> configViewItem(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    contextMenuItems: ((T) -> List<TabulatorMenuItem>)? = null,
    baseUrl: String? = null,
): ConfigViewItem<CC, T, ID, V, FILT, AIS> = object : ConfigViewItem<CC, T, ID, V, FILT, AIS>(
    commonContainer = commonContainer,
    apiItemFun = apiItemFun,
    viewKClass = viewKClass,
    contextMenuItems = contextMenuItems,
    baseUrl = baseUrl
) {}
