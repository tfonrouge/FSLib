package com.fonrouge.fullStack.config

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.view.KVWebManager.configViewListMap
import com.fonrouge.fullStack.view.ViewList
import dev.kilua.rpc.RpcServiceManager
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
 * @param FILT The API filter type, extending IApiFilter.
 * @param MID The type of the metadata ID used in filtering.
 * @param ALS The execution context for the API list function.
 * @param commonContainer An instance of the common container that manages the configuration and items of the list.
 * @param apiListFun A suspend function for retrieving a list state based on a given API filter.
 * @param viewKClass The KClass instance of the view list class, representing the specific view type.
 * @param baseUrl Optional; a custom base URL for the configuration. Defaults to the simple name of the view class.
 */
abstract class ConfigViewList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, FILT : IApiFilter<MID>, MID : Any, ALS : Any>(
    commonContainer: CC,
    val apiListFun: suspend ALS.(ApiList<FILT>) -> ListState<T>,
    viewKClass: KClass<V>,
    baseUrl: String? = null,
) : ConfigViewContainer<CC, T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {
    companion object {
        private var dataListServiceManager: RpcServiceManager<*>? = null

        /**
         * A delegating property used to manage the `RpcServiceManager` instance for the `ConfigViewList` class.
         *
         * - The `serviceManager` property must be explicitly initialized before any instance of `ConfigViewList` is created.
         * - Failing to initialize this property will result in an `IllegalStateException` being thrown when attempting to access it.
         *
         * This property internally delegates its value to `dataListServiceManager`.
         * If the value is not set, it throws an exception and shows an alert message with the exception details.
         *
         * @throws IllegalStateException if the property is accessed before it is initialized.
         *
         * @see RpcServiceManager
         */
        var serviceManager: RpcServiceManager<*>
            get() = dataListServiceManager
                ?: throw IllegalStateException("serviceManager is null. Please set ConfigViewList.serviceManager value before instantiating any ConfigViewList.".also {
                    alert(
                        it
                    )
                })
            set(value) {
                dataListServiceManager = value
            }
    }

    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    override val label: String get() = commonContainer.labelList
    override val labelUrl: Pair<String, String> by lazy { commonContainer.labelList to url }

    fun viewListUrl(apiFilter: FILT = commonContainer.apiFilterInstance()): String =
        urlWithParams(apiFilterParam(apiFilter))

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
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewList<CC, T, ID, FILT, MID>, FILT : IApiFilter<MID>, MID : Any, ALS : Any> configViewList(
    viewKClass: KClass<V>,
    commonContainer: CC,
    apiListFun: suspend ALS.(ApiList<FILT>) -> ListState<T>,
    baseUrl: String? = null,
): ConfigViewList<CC, T, ID, V, FILT, MID, ALS> = object : ConfigViewList<CC, T, ID, V, FILT, MID, ALS>(
    commonContainer = commonContainer,
    apiListFun = apiListFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
