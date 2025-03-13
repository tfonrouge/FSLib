package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import kotlinx.browser.window
import org.w3c.dom.Window
import web.prompts.alert
import kotlin.reflect.KClass

/**
 * Abstract class that provides a configuration for a view that handles listing and displaying collections of items,
 * extending the functionality of a container-base class.
 *
 * @param CC The type of the common container managing the API items and metadata, must extend ICommonContainer.
 * @param T The type of the items within the list, must implement BaseDoc.
 * @param ID The identifier type of the items in the list, must be non-nullable.
 * @param V The type of the view list class, extending ViewList.
 * @param E The execution context for the API list function.
 * @param FILT The API filter type, extending IApiFilter.
 * @param MID The type of the metadata ID used in filtering.
 * @param commonContainer An instance of the common container that manages the configuration and items of the list.
 * @param apiListFun A suspend function for retrieving a list state based on a given API filter.
 * @param viewKClass The KClass instance of the view list class, representing the specific view type.
 * @param baseUrl Optional; a custom base URL for the configuration. Defaults to the simple name of the view class.
 */
abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, E : Any, FILT : IApiFilter<MID>, MID : Any>(
    commonContainer: CC,
    val apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {
    companion object {
        private var dataListServiceManager: KVServiceManager<*>? = null
        var serviceManager: KVServiceManager<*>
            get() = dataListServiceManager
                ?: throw IllegalStateException("serviceManager is null. Please set ConfigViewList.serviceManager value before instantiating any ConfigViewList.".also {
                    alert(
                        it
                    )
                })
            set(value) {
                dataListServiceManager = value
            }
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *, *>>()
    }

    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    override val label: String get() = commonContainer.labelList
    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelList to url }

    /**
     * builds an url string with optional [IApiFilter] parameter
     */
    fun url(apiFilter: FILT? = null): String {
        return baseUrl + (apiFilter?.let {
            "?" + pairParam(
                key = "apiFilter",
                serializer = commonContainer.apiFilterSerializer,
                obj = apiFilter
            )
        } ?: "")
    }

    fun viewListUrl(apiFilter: FILT = commonContainer.apiFilterInstance()): String {
        val params = mutableListOf<Pair<String, String>>()
        apiFilterParam(apiFilter).let { params.add(it) }
        return urlWithParams(*params.toTypedArray())
    }

    /**
     * Navigates to a specific URL in a new or existing window.
     *
     * @param apiFilter An optional filter to apply to the URL, defaults to an instance of the filter.
     * @param target The target window or frame where the URL should be opened, defaults to "_blank".
     * @return The Window object of the opened URL, or null if the operation fails.
     */
    @Suppress("unused")
    fun navigateTo(
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        target: String = "_blank"
    ): Window? {
        return window.open(
            url = viewListUrl(apiFilter),
            target = target
        )
    }

    init {
        configViewListMap[this.baseUrl] = this
    }
}

/**
 * Configures a view list with the given parameters.
 *
 * This function creates and returns an instance of `ConfigViewList`, which serves as a configuration
 * for managing a view list for a specific combination of item, filter type, and view type in the application.
 *
 * @param viewKClass The KClass of the view list type being configured.
 * @param commonContainer The container managing the items and API-related details for the given type.
 * @param apiListFun A suspend function that defines the API calls to retrieve the list state for the specified filter.
 * @param baseUrl An optional base URL for the API interaction. Defaults to null.
 * @return An instance of `ConfigViewList` configured with the provided parameters.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, V : ViewList<CC, T, ID, FILT, MID>, E : Any, ID : Any, FILT : IApiFilter<MID>, MID : Any> configViewList(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, E, FILT, MID> = object : ConfigViewList<CC, T, ID, V, E, FILT, MID>(
    commonContainer = commonContainer,
    apiListFun = apiListFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
