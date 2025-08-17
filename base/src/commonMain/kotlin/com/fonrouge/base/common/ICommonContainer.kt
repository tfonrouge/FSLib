package com.fonrouge.base.common

import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * This abstract class defines a common container for managing API items of a certain type.
 *
 * @param T The type of items managed by the container, which must extend BaseDoc.
 * @param ID The type of the ID field of the items, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, must extend IApiFilter.
 * @property itemKClass Kotlin's KClass instance of the item type.
 * @property idSerializer Kotlinx serialization serializer for the ID type.
 * @property apiFilterSerializer Kotlinx serialization serializer for the API filter.
 * @property labelId Function to generate a label for the ID of an item.
 * @property labelItem Label for a single item.
 * @property labelItemId Function to generate a label for an item with its ID.
 * @property labelList Label for a list of items.
 */
abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
    override val apiFilterSerializer: KSerializer<FILT>,
    open val labelId: ((T?) -> String) = { t: T? -> t?.let { "${it._id}" } ?: "<no-item>" },
    open val labelItem: String = "${itemKClass.simpleName}",
    open val labelItemId: ((T?) -> String) = { t: T? -> "$labelItem: ${labelId(t)}" },
    open val labelList: String = "List of ${itemKClass.simpleName}",
) : ICommon<FILT>(
    apiFilterSerializer = apiFilterSerializer
) {
    @OptIn(InternalSerializationApi::class)
    val itemSerializer get() = itemKClass.serializer()

    /* ApiItem */
    fun apiItemQueryCreate(
        id: ID? = null,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Query.Create<T, ID, FILT> = ApiItem.Query.Create(
        id = id,
        apiFilter = apiFilter,
    )

    fun apiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Query.Read<T, ID, FILT> = ApiItem.Query.Read(
        id = id,
        apiFilter = apiFilter,
    )

    fun apiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Query.Update<T, ID, FILT> = ApiItem.Query.Update(
        id = id,
        apiFilter = apiFilter,
    )

    fun apiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Query.Delete<T, ID, FILT> = ApiItem.Query.Delete(
        id = id,
        apiFilter = apiFilter,
    )

    fun apiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Action.Create<T, ID, FILT> = ApiItem.Action.Create(
        item = item,
        apiFilter = apiFilter,
    )

    fun apiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Action.Update<T, ID, FILT> = ApiItem.Action.Update(
        item = item,
        apiFilter = apiFilter,
    )

    fun apiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
    ): ApiItem.Action.Delete<T, ID, FILT> = ApiItem.Action.Delete(
        item = item,
        apiFilter = apiFilter,
    )
}

/**
 * Converts an instance of [ApiItem] to an instance of [IApiItem].
 *
 * @param apiItem The API item to be converted.
 * @return The converted API item as an instance of [IApiItem].
 */
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ICommonContainer<T, ID, FILT>.toIApiItem(
    apiItem: ApiItem<T, ID, FILT>,
): IApiItem<T, ID, FILT> = when (apiItem) {
    is ApiItem.Query.Create -> IApiItem.Query.Create(
        serializedId = apiItem.id?.let { Json.encodeToString(idSerializer, it) },
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )

    is ApiItem.Query.Read -> IApiItem.Query.Read(
        serializedId = Json.encodeToString(idSerializer, apiItem.id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )

    is ApiItem.Query.Update -> IApiItem.Query.Update(
        serializedId = Json.encodeToString(idSerializer, apiItem.id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )

    is ApiItem.Query.Delete -> IApiItem.Query.Delete(
        serializedId = Json.encodeToString(idSerializer, apiItem.id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )

    is ApiItem.Action.Create -> IApiItem.Action.Create(
        serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )

    is ApiItem.Action.Update -> IApiItem.Action.Update(
        serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter),
    )

    is ApiItem.Action.Delete -> IApiItem.Action.Delete(
        serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
    )
}
