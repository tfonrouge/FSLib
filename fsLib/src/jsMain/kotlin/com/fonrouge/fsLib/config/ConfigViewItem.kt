package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.remote.KVServiceManager
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import kotlin.reflect.KClass

/**
 * Represents an abstract base class for configuring a view item in the system.
 * This class provides utility functions for creating, reading, updating, deleting,
 * and navigating to specific URLs associated with the items it manages.
 *
 * @param CC The type representing the common container that manages the shared configurations and properties.
 * @param T The type representing the document or data model managed by the view item.
 * @param ID The type representing the unique identifier for the document or data model.
 * @param V The type representing the specific view item class to be used.
 * @param FILT The type representing the filter applicable to API calls.
 * @param AIS The type representing the API service used for backend interactions.
 * @property serviceManager Manages API service interactions for this view item.
 * @property apiItemFun A suspendable function that performs API operations on the given API item and returns its state.
 * @constructor Initializes the class with necessary parameters for managing view items, including the common container,
 *              service manager, API interaction function, and additional configurations such as the base URL.
 */
abstract class ConfigViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any>(
    commonContainer: CC,
    val serviceManager: KVServiceManager<AIS>,
    val apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl,
) {
    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    companion object {
        val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()
    }

    override val label: String get() = commonContainer.labelItem

    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelItem to url }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = commonContainer.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = commonContainer.labelItem to urlUpdate(id)

    /**
     * Opens a new browser window or tab with the URL corresponding to the given ApiItem.
     *
     * @param apiItem The ApiItem representing the query to be executed.
     * @param target The target where the URL should be opened. Defaults to "_blank".
     * @return The window object of the newly opened window/tab, or null if the URL cannot be generated.
     */
    @Suppress("unused")
    fun navigateTo(apiItem: ApiItem<T, ID, FILT>, target: String = "_blank"): Window? {
        return viewItemUrl(apiItem)?.let { url ->
            window.open(
                url = url,
                target = target
            )
        }
    }

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
            is ApiItem.Upsert.Create.Query -> listOf("action" to CrudTask.Create.name)
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
 * Configures a view item with the specified parameters, establishing interactions
 * between the front-end view and the underlying service, container, and API logic.
 *
 * @param viewKClass The KClass instance of the view item being configured.
 * @param commonContainer The common container responsible for managing items of the specified type.
 * @param serviceManager The service manager providing access to the API common service.
 * @param apiItemFun A suspend function defining the API interaction for processing items.
 * @param baseUrl An optional base URL used as part of the configuration.
 * @return A ConfigViewItem instance configured with the provided parameters.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, FILT : IApiFilter<*>, AIS : Any> configViewItem(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    baseUrl: String? = null
): ConfigViewItem<CC, T, ID, V, FILT, AIS> = object : ConfigViewItem<CC, T, ID, V, FILT, AIS>(
    commonContainer = commonContainer,
    serviceManager = serviceManager,
    apiItemFun = apiItemFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
