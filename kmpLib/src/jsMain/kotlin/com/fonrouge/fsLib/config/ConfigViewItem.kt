package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.commonServices.IApiCommonService
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
 * An abstract class representing a configuration view item. This class manages the configuration
 * details, URL generation, and view item interactions for a specific type of data container.
 *
 * @param CC The type of the common container associated with this configuration view item.
 * @param T The type of document this view item is working with.
 * @param ID The type of unique identifier used for the document.
 * @param V The type of the child view item class associated with this configuration view item.
 * @param AIS The API service type used for handling data operations.
 * @param FILT The type of filter used for querying the API.
 * @param configData The configuration data item, which holds core container information.
 * @param viewKClass The KClass of the view item.
 * @param baseUrl The base URL for the configuration view item, can be null.
 */
abstract class ConfigViewItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiCommonService, FILT : IApiFilter<*>>(
    override val configData: ConfigDataItem<CC, T, ID, FILT, AIS>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    configData = configData,
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

    override val label: String get() = configData.commonContainer.labelItem

    override val labelUrl: Pair<String, String> by lazy { configData.commonContainer.labelItem to url }

    @Suppress("unused")
    fun labelUrlRead(id: ID) = configData.commonContainer.labelItem to urlRead(id)

    @Suppress("unused")
    fun labelUrlUpdate(id: ID) = configData.commonContainer.labelItem to urlUpdate(id)

    val urlCreate: String
        get() {
            val urlParams = UrlParams("action" to CrudTask.Create.name)
            return url + urlParams.toEncodedUrlString()
        }

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

    fun urlRead(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, id),
                "action" to CrudTask.Read.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    @Suppress("unused")
    fun urlDelete(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, id),
                "action" to CrudTask.Delete.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun urlUpdate(id: ID): String {
        val urlParams =
            UrlParams(
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, id),
                "action" to CrudTask.Update.name
            )
        return url + urlParams.toEncodedUrlString()
    }

    fun viewItemUrl(apiItem: ApiItem<T, ID, FILT>): String? {
        val url: String? = when (apiItem) {
            is ApiItem.Upsert.Create.Query -> listOf("action" to CrudTask.Create.name)
            is ApiItem.Read -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Upsert.Update.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, apiItem.id)
            )

            is ApiItem.Delete.Query -> listOf(
                "action" to apiItem.crudTask.name,
                "id" to Json.encodeToString(configData.commonContainer.idSerializer, apiItem.id)
            )

            else -> null
        }?.let { params: List<Pair<String, String>> ->
            val urlParams = UrlParams(*params.toTypedArray())
            urlParams.pushParam(
                "apiFilter" to Json.encodeToString(
                    configData.commonContainer.apiFilterSerializer,
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
 * Configures a view item by associating it with a container, service manager, and relevant API logic.
 *
 * @param viewKClass The Kotlin class of the view item to be configured.
 * @param commonContainer The common container instance managing the items.
 * @param serviceManager The service manager that provides API services for the operation.
 * @param apiItemFun A suspend function defining API-based operations for an item. Takes an API Item and returns the item's state.
 * @param baseUrl An optional base URL to be used for constructing routes or API endpoints.
 * @return A `ConfigViewItem` instance associating the specified view with its data, services, and configuration.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewItem<CC, T, ID, FILT>, AIS : IApiCommonService, FILT : IApiFilter<*>> configViewItem(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    baseUrl: String? = null
): ConfigViewItem<CC, T, ID, V, AIS, FILT> = object : ConfigViewItem<CC, T, ID, V, AIS, FILT>(
    configData = configDataItem<CC, T, ID, FILT, AIS>(
        commonContainer = commonContainer,
        serviceManager = serviceManager,
        apiItemFun = apiItemFun,
    ),
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
