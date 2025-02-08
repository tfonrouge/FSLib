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
 * Abstract class representing a configuration for a view list.
 *
 * @param CC The type of the common container managing the related API items.
 * @param T The type of items managed in the list, which extends BaseDoc.
 * @param ID The type of the unique identifier for the items, which must be non-nullable.
 * @param V The type of the view list representation.
 * @param E The type of additional configuration data or extension.
 * @param FILT The type of the API filter used for querying the items, extending IApiFilter.
 * @param MID The type of metadata identifier used for filtering.
 * @property configData Configuration data specific to list views.
 * @property viewKClass The KClass reference of the specific view list type.
 * @property baseUrl Optional base URL, falling back to the name of the view class if not provided.
 */
abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, E : Any, FILT : IApiFilter<MID>, MID : Any>(
    override val configData: ConfigDataList<CC, T, ID, E, FILT, MID>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    configData = configData,
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

    override val label: String get() = configData.commonContainer.labelList
    override val labelUrl: Pair<String, String> by lazy { configData.commonContainer.labelList to url }

    /**
     * builds an url string with optional [IApiFilter] parameter
     */
    fun url(apiFilter: FILT? = null): String {
        return baseUrl + (apiFilter?.let {
            "?" + pairParam(
                key = "apiFilter",
                serializer = configData.commonContainer.apiFilterSerializer,
                obj = apiFilter
            )
        } ?: "")
    }

    fun viewListUrl(apiFilter: FILT = configData.commonContainer.apiFilterInstance()): String {
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
        apiFilter: FILT = configData.commonContainer.apiFilterInstance(),
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
 * Configures a view list with the specified parameters, linking a view class, a common container,
 * a service manager, an API function for fetching the list, and an optional base URL.
 *
 * @param viewKClass The Kotlin class of the view that needs configuration.
 * @param commonContainer An instance of the common container that manages the data model.
 * @param serviceManager The service manager responsible for handling the service logic and API interaction.
 * @param apiListFun A suspend function defining the API call for fetching a list of items,
 *                   which takes an API filter and returns a list state.
 * @param baseUrl An optional parameter specifying the base URL for API requests. Defaults to null.
 * @return A configured instance of ConfigViewList with the provided parameters.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, V : ViewList<CC, T, ID, FILT, MID>, E : Any, ID : Any, FILT : IApiFilter<MID>, MID : Any> configViewList(
    viewKClass: KClass<out V>,
    commonContainer: CC,
    serviceManager: KVServiceManager<E>,
    apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, E, FILT, MID> = object : ConfigViewList<CC, T, ID, V, E, FILT, MID>(
    configData = configDataList<CC, T, ID, E, FILT, MID>(
        commonContainer = commonContainer,
        serviceManager = serviceManager,
        apiListFun = apiListFun,
    ),
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
