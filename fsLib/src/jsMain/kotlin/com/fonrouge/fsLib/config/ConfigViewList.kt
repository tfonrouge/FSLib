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
import kotlin.reflect.KClass

/**
 * ConfigViewList is an abstract class that provides configuration and utilities for managing
 * a list view of items associated with a specific container. It facilitates interaction with
 * APIs, URL construction, and navigation for views.
 *
 * @param CC The type of the common container, which must extend ICommonContainer.
 * @param T The type of items managed by the view, which must extend BaseDoc.
 * @param ID The type of the ID of the items in the view, which must be a non-nullable type.
 * @param V The type of the view class associated with the container.
 * @param E The type of the service manager that provides services for API interaction.
 * @param FILT The type of API filter used for querying, must extend IApiFilter.
 * @param MID The type of the filter metadata used by the API filter.
 * @param commonContainer The common container instance managing the configured items.
 * @param serviceManager The service manager that enables communication with API services.
 * @param apiListFun A suspend function provided by the service manager to fetch a list of items
 *                   corresponding to a specific filter.
 * @param viewKClass The KClass instance of the view associated with the configuration.
 * @param baseUrl Optional base URL for the view; defaults to the simple name of the view class.
 */
abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, E : Any, FILT : IApiFilter<MID>, MID : Any>(
    commonContainer: CC,
    val serviceManager: KVServiceManager<E>,
    val apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {
    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    companion object {
        val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *, *>>()
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
 * Configures a view list component for managing and rendering a collection of items, where the component is tied to
 * a specific container, API service manager, and filtering logic.
 *
 * @param viewKClass The KClass type of the view list component.
 * @param commonContainer The container managing the items and their lifecycle.
 * @param serviceManager The service manager responsible for backend interactions.
 * @param apiListFun The API function for fetching a filtered list of items.
 * @param baseUrl An optional base URL for API requests. Defaults to null.
 * @return A configured instance of ConfigViewList.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, V : ViewList<CC, T, ID, FILT, MID>, E : Any, ID : Any, FILT : IApiFilter<MID>, MID : Any> configViewList(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    serviceManager: KVServiceManager<E>,
    apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, E, FILT, MID> = object : ConfigViewList<CC, T, ID, V, E, FILT, MID>(
    commonContainer = commonContainer,
    serviceManager = serviceManager,
    apiListFun = apiListFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
