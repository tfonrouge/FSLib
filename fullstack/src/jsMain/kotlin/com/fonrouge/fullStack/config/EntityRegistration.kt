package com.fonrouge.fullStack.config

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.tabulator.TabulatorMenuItem
import com.fonrouge.fullStack.view.ViewItem
import com.fonrouge.fullStack.view.ViewList
import dev.kilua.rpc.RpcServiceManager
import kotlin.reflect.KClass

/**
 * Builder for declaratively registering entity view configurations.
 *
 * Collects list and item view registrations via [list] and [item], tracks the default view,
 * and sets up [ViewRegistry] service managers. Use via [registerEntityViews].
 *
 * Example:
 * ```kotlin
 * val reg = registerEntityViews(getServiceManager<ITaskService>()) {
 *     list(ViewListTask::class, CommonTask, ITaskService::apiList, isDefault = true)
 *     item(ViewItemTask::class, CommonTask, ITaskService::apiItem)
 * }
 * KVWebManager.initialize { defaultView = reg.defaultView }
 * ```
 */
class EntityRegistrationBuilder {
    private var _defaultView: ConfigView<*, *>? = null

    /**
     * The view marked as default via `isDefault = true`, or null if none was marked.
     */
    val defaultView: ConfigView<*, *>? get() = _defaultView

    /**
     * Registers a list view configuration for an entity.
     *
     * @param T The entity type.
     * @param ID The identifier type.
     * @param V The [ViewList] subclass.
     * @param FILT The filter type.
     * @param MID The master item ID type used in the filter.
     * @param ALS The RPC service type providing the list endpoint.
     * @param viewKClass The [KClass] of the list view.
     * @param commonContainer The entity's metadata container.
     * @param apiListFun The RPC function for list retrieval.
     * @param baseUrl Optional custom base URL for the view.
     * @param isDefault Whether this view should be set as the default view.
     * @return The created [ConfigViewList] instance.
     */
    fun <T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, FILT, MID>,
            FILT : IApiFilter<MID>, MID : Any, ALS : Any> list(
        viewKClass: KClass<V>,
        commonContainer: ICommonContainer<T, ID, FILT>,
        apiListFun: suspend ALS.(ApiList<FILT>) -> ListState<T>,
        baseUrl: String? = null,
        isDefault: Boolean = false,
    ): ConfigViewList<T, ID, V, FILT, MID, ALS> {
        val config = configViewList(
            viewKClass = viewKClass,
            commonContainer = commonContainer,
            apiListFun = apiListFun,
            baseUrl = baseUrl,
        )
        if (isDefault) {
            _defaultView = config
        }
        return config
    }

    /**
     * Registers an item view configuration for an entity.
     *
     * @param T The entity type.
     * @param ID The identifier type.
     * @param V The [ViewItem] subclass.
     * @param FILT The filter type.
     * @param AIS The RPC service type providing the item endpoint.
     * @param viewKClass The [KClass] of the item view.
     * @param commonContainer The entity's metadata container.
     * @param apiItemFun The RPC function for item CRUD operations.
     * @param contextMenuItems Optional function providing context menu items for grid rows.
     * @param baseUrl Optional custom base URL for the view.
     * @param isDefault Whether this view should be set as the default view.
     * @return The created [ConfigViewItem] instance.
     */
    fun <T : BaseDoc<ID>, ID : Any, V : ViewItem<T, ID, FILT>,
            FILT : IApiFilter<*>, AIS : Any> item(
        viewKClass: KClass<out V>,
        commonContainer: ICommonContainer<T, ID, FILT>,
        apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
        contextMenuItems: ((T) -> List<TabulatorMenuItem>)? = null,
        baseUrl: String? = null,
        isDefault: Boolean = false,
    ): ConfigViewItem<T, ID, V, FILT, AIS> {
        val config = configViewItem(
            viewKClass = viewKClass,
            commonContainer = commonContainer,
            apiItemFun = apiItemFun,
            contextMenuItems = contextMenuItems,
            baseUrl = baseUrl,
        )
        if (isDefault) {
            _defaultView = config
        }
        return config
    }
}

/**
 * Registers entity views using a declarative DSL, setting up [ViewRegistry] service managers
 * and creating view configurations in a single block.
 *
 * Uses a single [RpcServiceManager] for both item and list operations (common case).
 *
 * @param serviceManager The RPC service manager shared by all views.
 * @param block The DSL block where [EntityRegistrationBuilder.list] and [EntityRegistrationBuilder.item] calls register views.
 * @return The builder, whose [EntityRegistrationBuilder.defaultView] can be passed to [com.fonrouge.fullStack.view.KVWebManager.initialize].
 */
fun registerEntityViews(
    serviceManager: RpcServiceManager<*>,
    block: EntityRegistrationBuilder.() -> Unit,
): EntityRegistrationBuilder {
    ViewRegistry.itemServiceManager = serviceManager
    ViewRegistry.listServiceManager = serviceManager
    return EntityRegistrationBuilder().apply(block)
}

/**
 * Registers entity views using a declarative DSL with separate service managers
 * for item and list operations.
 *
 * Use this overload when the item and list RPC endpoints are served by different
 * service interfaces (e.g., the Arel pattern).
 *
 * @param itemServiceManager The RPC service manager for item operations.
 * @param listServiceManager The RPC service manager for list operations.
 * @param block The DSL block where [EntityRegistrationBuilder.list] and [EntityRegistrationBuilder.item] calls register views.
 * @return The builder, whose [EntityRegistrationBuilder.defaultView] can be passed to [com.fonrouge.fullStack.view.KVWebManager.initialize].
 */
fun registerEntityViews(
    itemServiceManager: RpcServiceManager<*>,
    listServiceManager: RpcServiceManager<*>,
    block: EntityRegistrationBuilder.() -> Unit,
): EntityRegistrationBuilder {
    ViewRegistry.itemServiceManager = itemServiceManager
    ViewRegistry.listServiceManager = listServiceManager
    return EntityRegistrationBuilder().apply(block)
}
