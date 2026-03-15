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
): ICommonContainer<T, ID, ApiFilter> = object : ICommonContainer<T, ID, ApiFilter>(
    itemKClass = T::class,
    filterKClass = ApiFilter::class,
    labelItem = labelItem,
    labelList = labelList,
    labelId = labelId,
    labelItemId = labelItemId,
) {}

/**
 * Creates a simple [ICommonContainer] for entities using a custom filter type.
 *
 * Use this variant when the entity requires a filter type other than the default [ApiFilter].
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
): ICommonContainer<T, ID, FILT> = object : ICommonContainer<T, ID, FILT>(
    itemKClass = T::class,
    filterKClass = FILT::class,
    labelItem = labelItem,
    labelList = labelList,
    labelId = labelId,
    labelItemId = labelItemId,
) {}
