package com.fonrouge.fullStack.config

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.common.ICommon
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.tabulator.TabulatorMenuItem
import com.fonrouge.fullStack.view.View
import com.fonrouge.fullStack.view.ViewItem
import com.fonrouge.fullStack.view.ViewList
import dev.kilua.rpc.RpcServiceManager
import kotlin.reflect.KClass

/**
 * Builder for declaratively registering entity view configurations.
 *
 * Provides two registration modes:
 * - **Reference-based:** pass an existing [ConfigViewList] / [ConfigViewItem] (e.g., from a companion object).
 *   This avoids double-registration and is the recommended approach when view classes already define their config.
 * - **Inline creation:** pass a [KClass], container, and RPC function to create and register a new config.
 *   Useful when you want the DSL to be the sole owner of the config (no companion objects).
 *
 * Use via [registerEntityViews].
 *
 * Example (reference-based — recommended when views have companion configs):
 * ```kotlin
 * val reg = registerEntityViews(getServiceManager<ITaskService>()) {
 *     list(ViewListTask.configViewList, isDefault = true)
 *     item(ViewItemTask.configViewItem)
 * }
 * KVWebManager.initialize { defaultView = reg.defaultView }
 * ```
 *
 * Example (inline creation — when views don't have companion configs):
 * ```kotlin
 * val reg = registerEntityViews(getServiceManager<ITaskService>()) {
 *     list(ViewListTask::class, CommonTask, ITaskService::apiList, isDefault = true)
 *     item(ViewItemTask::class, CommonTask, ITaskService::apiItem)
 * }
 * ```
 */
class EntityRegistrationBuilder {
    private var _defaultView: ConfigView<*, *>? = null

    /**
     * The view marked as default via `isDefault = true`, or null if none was marked.
     */
    val defaultView: ConfigView<*, *>? get() = _defaultView

    @PublishedApi
    internal fun setDefault(config: ConfigView<*, *>) {
        if (_defaultView != null) {
            console.warn(
                "registerEntityViews: multiple views marked as isDefault=true. " +
                        "Overwriting '${_defaultView!!.baseUrl}' with '${config.baseUrl}'."
            )
        }
        _defaultView = config
    }

    // ── Non-data view registration ─────────────────────────────

    /**
     * Registers an existing [ConfigView] for a non-data view (landing page, dashboard, etc.)
     * and optionally marks it as the default view.
     *
     * @param config The existing [ConfigView] instance.
     * @param isDefault Whether this view should be set as the default view.
     * @return The same [ConfigView] instance, for chaining.
     */
    fun <V : View<FILT>, FILT : IApiFilter<*>> view(
        config: ConfigView<V, FILT>,
        isDefault: Boolean = false,
    ): ConfigView<V, FILT> {
        if (isDefault) setDefault(config)
        return config
    }

    /**
     * Creates and registers a new [ConfigView] for a non-data view (landing page, dashboard, etc.).
     *
     * Use this when the view has no companion-object config. The view class must extend
     * [View] directly (not [com.fonrouge.fullStack.view.ViewDataContainer]).
     *
     * @param V The [View] subclass.
     * @param FILT The filter type (can represent view state).
     * @param viewKClass The [KClass] of the view.
     * @param commonContainer An [ICommon] instance providing label and filter metadata.
     * @param baseUrl Optional custom base URL for the view.
     * @param isDefault Whether this view should be set as the default view.
     * @return The created [ConfigView] instance.
     */
    inline fun <V : View<FILT>, reified FILT : IApiFilter<*>> view(
        viewKClass: KClass<out V>,
        commonContainer: ICommon<FILT>,
        baseUrl: String? = null,
        isDefault: Boolean = false,
    ): ConfigView<V, FILT> {
        val config = configView(
            viewKClass = viewKClass,
            commonContainer = commonContainer,
            baseUrl = baseUrl,
        )
        if (isDefault) setDefault(config)
        return config
    }

    // ── Reference-based registration (recommended) ──────────────

    /**
     * Registers an existing [ConfigViewList] and optionally marks it as the default view.
     *
     * Use this when the view class already defines its config in a companion object.
     * No new config instance is created — this simply tracks the default view.
     *
     * @param config The existing [ConfigViewList] instance (e.g., `ViewListTask.configViewList`).
     * @param isDefault Whether this view should be set as the default view.
     * @return The same [ConfigViewList] instance, for chaining.
     */
    fun <T : BaseDoc<ID>, ID : Any, V : ViewList<T, ID, FILT, MID>,
            FILT : IApiFilter<MID>, MID : Any, ALS : Any> list(
        config: ConfigViewList<T, ID, V, FILT, MID, ALS>,
        isDefault: Boolean = false,
    ): ConfigViewList<T, ID, V, FILT, MID, ALS> {
        if (isDefault) setDefault(config)
        return config
    }

    /**
     * Registers an existing [ConfigViewItem] and optionally marks it as the default view.
     *
     * Use this when the view class already defines its config in a companion object.
     * No new config instance is created — this simply tracks the default view.
     *
     * @param config The existing [ConfigViewItem] instance (e.g., `ViewItemTask.configViewItem`).
     * @param isDefault Whether this view should be set as the default view.
     * @return The same [ConfigViewItem] instance, for chaining.
     */
    fun <T : BaseDoc<ID>, ID : Any, V : ViewItem<T, ID, FILT>,
            FILT : IApiFilter<*>, AIS : Any> item(
        config: ConfigViewItem<T, ID, V, FILT, AIS>,
        isDefault: Boolean = false,
    ): ConfigViewItem<T, ID, V, FILT, AIS> {
        if (isDefault) setDefault(config)
        return config
    }

    // ── Inline creation registration ────────────────────────────

    /**
     * Creates and registers a new [ConfigViewList] for an entity.
     *
     * Use this when the view class does **not** define a companion-object config.
     * A new config instance is created and auto-registered into [ViewRegistry].
     *
     * **Note:** If the view class also has a companion-object config for the same [viewKClass],
     * both will register under the same base URL — the last one initialized wins.
     * Prefer the reference-based [list] overload to avoid this.
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
        if (isDefault) setDefault(config)
        return config
    }

    /**
     * Creates and registers a new [ConfigViewItem] for an entity.
     *
     * Use this when the view class does **not** define a companion-object config.
     * A new config instance is created and auto-registered into [ViewRegistry].
     *
     * **Note:** If the view class also has a companion-object config for the same [viewKClass],
     * both will register under the same base URL — the last one initialized wins.
     * Prefer the reference-based [item] overload to avoid this.
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
        if (isDefault) setDefault(config)
        return config
    }
}

/**
 * Registers entity views using a declarative DSL, setting up [ViewRegistry] service managers
 * and creating view configurations in a single block.
 *
 * Uses a single [RpcServiceManager] for both item and list operations (common case).
 *
 * If [ViewRegistry] service managers have already been set to **different** values,
 * a warning is logged before overwriting them.
 *
 * @param serviceManager The RPC service manager shared by all views.
 * @param block The DSL block where [EntityRegistrationBuilder.list] and [EntityRegistrationBuilder.item] calls register views.
 * @return The builder, whose [EntityRegistrationBuilder.defaultView] can be passed to [com.fonrouge.fullStack.view.KVWebManager.initialize].
 */
fun registerEntityViews(
    serviceManager: RpcServiceManager<*>,
    block: EntityRegistrationBuilder.() -> Unit,
): EntityRegistrationBuilder {
    setServiceManagers(serviceManager, serviceManager)
    return EntityRegistrationBuilder().apply(block)
}

/**
 * Registers entity views using a declarative DSL with separate service managers
 * for item and list operations.
 *
 * Use this overload when the item and list RPC endpoints are served by different
 * service interfaces (e.g., the Arel pattern).
 *
 * If [ViewRegistry] service managers have already been set to **different** values,
 * a warning is logged before overwriting them.
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
    setServiceManagers(itemServiceManager, listServiceManager)
    return EntityRegistrationBuilder().apply(block)
}

/**
 * Sets service managers on [ViewRegistry], warning if they are being overwritten
 * with different values (which usually indicates a configuration mistake).
 */
private fun setServiceManagers(
    itemSM: RpcServiceManager<*>,
    listSM: RpcServiceManager<*>,
) {
    val currentItemSM = runCatching { ViewRegistry.itemServiceManager }.getOrNull()
    val currentListSM = runCatching { ViewRegistry.listServiceManager }.getOrNull()

    if (currentItemSM != null && currentItemSM !== itemSM) {
        console.warn(
            "registerEntityViews: overwriting ViewRegistry.itemServiceManager — " +
                    "was this intentional? Consider using a single registerEntityViews() call."
        )
    }
    if (currentListSM != null && currentListSM !== listSM) {
        console.warn(
            "registerEntityViews: overwriting ViewRegistry.listServiceManager — " +
                    "was this intentional? Consider using a single registerEntityViews() call."
        )
    }

    ViewRegistry.itemServiceManager = itemSM
    ViewRegistry.listServiceManager = listSM
}
