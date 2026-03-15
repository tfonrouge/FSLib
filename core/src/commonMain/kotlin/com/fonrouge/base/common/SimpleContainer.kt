package com.fonrouge.base.common

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.model.BaseDoc

/**
 * Creates a simple [ICommonContainer] for entities using the default [ApiFilter].
 *
 * This factory eliminates the need for explicit `itemKClass` / `filterKClass` parameters
 * by using reified generics to infer them automatically.
 *
 * The returned container overrides [ICommon.name] to return the entity class simple name
 * (e.g., `"Task"` for `Task::class`), so URL generation and logging work correctly even
 * though the result is an anonymous object rather than a named `object` declaration.
 *
 * Example:
 * ```kotlin
 * val CommonTask = simpleContainer<Task, String>(
 *     labelItem = "Task",
 *     labelList = "Tasks",
 *     labelId = { it?.let { "${it.title} (${it._id})" } ?: "<no-task>" },
 * )
 * ```
 *
 * @param T The entity type, must implement [BaseDoc].
 * @param ID The identifier type.
 * @param labelItem Display label for a single item. Defaults to the class simple name.
 * @param labelList Display label for a list of items. Defaults to "List of {simpleName}".
 * @param labelId Function producing a display label from an item's identity. Defaults to showing `_id`.
 * @param labelItemId Function producing a combined item+id label. Defaults to "{labelItem}: {labelId}".
 * @return A new [ICommonContainer] instance configured with the given parameters.
 */
inline fun <reified T : BaseDoc<ID>, ID : Any> simpleContainer(
    labelItem: String = T::class.simpleName ?: "Item",
    labelList: String = "List of ${T::class.simpleName}",
    noinline labelId: ((T?) -> String) = { it?.let { "${it._id}" } ?: "<no-item>" },
    noinline labelItemId: ((T?) -> String) = { "$labelItem: ${labelId(it)}" },
): ICommonContainer<T, ID, ApiFilter> {
    val entityName = T::class.simpleName ?: "?"
    return object : ICommonContainer<T, ID, ApiFilter>(
        itemKClass = T::class,
        filterKClass = ApiFilter::class,
        labelItem = labelItem,
        labelList = labelList,
        labelId = labelId,
        labelItemId = labelItemId,
    ) {
        override val name: String get() = entityName
    }
}

/**
 * Creates a simple [ICommonContainer] for entities using a custom filter type.
 *
 * Use this variant when the entity requires a filter type other than the default [ApiFilter].
 *
 * The returned container overrides [ICommon.name] to return the entity class simple name,
 * ensuring correct URL generation despite being an anonymous object.
 *
 * Example:
 * ```kotlin
 * val CommonOrder = simpleContainerWithFilter<Order, String, OrderFilter>(
 *     labelItem = "Order",
 *     labelList = "Orders",
 * )
 * ```
 *
 * @param T The entity type, must implement [BaseDoc].
 * @param ID The identifier type.
 * @param FILT The custom filter type, must implement [IApiFilter].
 * @param labelItem Display label for a single item. Defaults to the class simple name.
 * @param labelList Display label for a list of items. Defaults to "List of {simpleName}".
 * @param labelId Function producing a display label from an item's identity. Defaults to showing `_id`.
 * @param labelItemId Function producing a combined item+id label. Defaults to "{labelItem}: {labelId}".
 * @return A new [ICommonContainer] instance configured with the given parameters.
 */
inline fun <reified T : BaseDoc<ID>, ID : Any, reified FILT : IApiFilter<*>> simpleContainerWithFilter(
    labelItem: String = T::class.simpleName ?: "Item",
    labelList: String = "List of ${T::class.simpleName}",
    noinline labelId: ((T?) -> String) = { it?.let { "${it._id}" } ?: "<no-item>" },
    noinline labelItemId: ((T?) -> String) = { "$labelItem: ${labelId(it)}" },
): ICommonContainer<T, ID, FILT> {
    val entityName = T::class.simpleName ?: "?"
    return object : ICommonContainer<T, ID, FILT>(
        itemKClass = T::class,
        filterKClass = FILT::class,
        labelItem = labelItem,
        labelList = labelList,
        labelId = labelId,
        labelItemId = labelItemId,
    ) {
        override val name: String get() = entityName
    }
}

/**
 * Creates a simple [ICommon] instance for non-data views (landing pages, dashboards, settings).
 *
 * Unlike [simpleContainer], this does **not** produce an [ICommonContainer] — there is no
 * associated data model, serializers, or CRUD factory methods. The result is a lightweight
 * metadata object carrying only a label and a filter serializer, suitable for use with
 * [com.fonrouge.fullStack.config.ConfigView] and [com.fonrouge.fullStack.view.View].
 *
 * The filter type can be used to represent view state (e.g., which tab is active,
 * which date range is selected) without implying a persistent data model.
 *
 * Example:
 * ```kotlin
 * val CommonHome = simpleCommon(label = "Home")
 * ```
 *
 * @param label Display label for the view. Defaults to `"View"`.
 * @param name Logical name used for URL generation. Defaults to [label].
 * @return A new [ICommon] instance configured with the default [ApiFilter].
 */
fun simpleCommon(
    label: String = "View",
    name: String = label,
): ICommon<ApiFilter> = object : ICommon<ApiFilter>(
    label = label,
    filterKClass = ApiFilter::class,
) {
    override val name: String get() = name
}

/**
 * Creates a simple [ICommon] instance for non-data views with a custom filter type.
 *
 * Use this variant when the view needs a typed filter for state management
 * (e.g., a dashboard filter with date range, selected tab, etc.).
 *
 * Example:
 * ```kotlin
 * val CommonDashboard = simpleCommonWithFilter<DashboardFilter>(label = "Dashboard")
 * ```
 *
 * @param FILT The custom filter type, must implement [IApiFilter].
 * @param label Display label for the view. Defaults to `"View"`.
 * @param name Logical name used for URL generation. Defaults to [label].
 * @return A new [ICommon] instance configured with the given filter type.
 */
inline fun <reified FILT : IApiFilter<*>> simpleCommonWithFilter(
    label: String = "View",
    name: String = label,
): ICommon<FILT> = object : ICommon<FILT>(
    label = label,
    filterKClass = FILT::class,
) {
    override val name: String get() = name
}
