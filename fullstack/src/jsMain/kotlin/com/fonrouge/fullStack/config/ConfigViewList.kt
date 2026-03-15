package com.fonrouge.fullStack.config

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.view.KVWebManager.configViewListMap
import com.fonrouge.fullStack.view.ViewList
import dev.kilua.rpc.RpcServiceManager
import kotlin.reflect.KClass

/**
 * Abstract class that provides a configuration for a view that handles listing and displaying collections of items,
 * extending the functionality of a container-base class.
 *
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
abstract class ConfigViewList<T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, FILT, MID>, FILT : IApiFilter<MID>, MID : Any, ALS : Any>(
    commonContainer: ICommonContainer<T, ID, FILT>,
    val apiListFun: suspend ALS.(ApiList<FILT>) -> ListState<T>,
    viewKClass: KClass<V>,
    baseUrl: String? = null,
) : ConfigViewContainer<T, ID, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {
    companion object {
        /**
         * RPC service manager for list operations. Delegated to [ViewRegistry.listServiceManager].
         *
         * @throws IllegalStateException if accessed before being initialized.
         */
        var serviceManager: RpcServiceManager<*>
            get() = ViewRegistry.listServiceManager
            set(value) {
                ViewRegistry.listServiceManager = value
            }
    }

    override val baseUrl: String
        get() {
            return _baseUrl ?: viewKClass.simpleName!!
        }

    override val label: String get() = commonContainer.labelList

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
fun <T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, FILT, MID>, FILT : IApiFilter<MID>, MID : Any, ALS : Any> configViewList(
    viewKClass: KClass<V>,
    commonContainer: ICommonContainer<T, ID, FILT>,
    apiListFun: suspend ALS.(ApiList<FILT>) -> ListState<T>,
    baseUrl: String? = null,
): ConfigViewList<T, ID, V, FILT, MID, ALS> = object : ConfigViewList<T, ID, V, FILT, MID, ALS>(
    commonContainer = commonContainer,
    apiListFun = apiListFun,
    viewKClass = viewKClass,
    baseUrl = baseUrl
) {}
