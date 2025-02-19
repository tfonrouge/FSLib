package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
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
 * @property labelIdFunc Function to generate a label for the ID of an item.
 * @property labelItem Label for a single item.
 * @property labelItemId Function to generate a label for an item with its ID.
 * @property labelList Label for a list of items.
 */
abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
    override val apiFilterSerializer: KSerializer<FILT>,
    open val labelIdFunc: ((T?) -> String) = { t: T? -> t?.let { "${it._id}" } ?: "<no-item>" },
    open val labelItem: String = "${itemKClass.simpleName}",
    open val labelItemId: ((T?) -> String) = { t: T? -> "$labelItem: ${labelIdFunc(t)}" },
    open val labelList: String = "List of ${itemKClass.simpleName}",
) : ICommon<FILT>(
    apiFilterSerializer = apiFilterSerializer
) {
    @OptIn(InternalSerializationApi::class)
    val itemSerializer get() = itemKClass.serializer()

    /* ApiItem */
    @Suppress("unused")
    fun apiItemQueryCreate(
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Upsert.Create.Query<T, ID, FILT> = ApiItem.Upsert.Create.Query(
        apiFilter = apiFilter,
    )

    @Suppress("unused")
    fun apiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Read<T, ID, FILT> = ApiItem.Read(
        id = id,
        apiFilter = apiFilter,
    )

    @Suppress("unused")
    fun apiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Upsert.Update.Query<T, ID, FILT> = ApiItem.Upsert.Update.Query(
        id = id,
        apiFilter = apiFilter,
    )

    @Suppress("unused")
    fun apiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Delete.Query<T, ID, FILT> = ApiItem.Delete.Query(
        id = id,
        apiFilter = apiFilter,
    )

    @Suppress("unused")
    fun apiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Upsert.Create.Action<T, ID, FILT> = ApiItem.Upsert.Create.Action(
        item = item,
        apiFilter = apiFilter,
    )

    @Suppress("unused")
    fun apiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
        orig: T?
    ): ApiItem.Upsert.Update.Action<T, ID, FILT> = ApiItem.Upsert.Update.Action(
        item = item,
        apiFilter = apiFilter,
        orig = orig,
    )

    @Suppress("unused")
    fun apiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Delete.Action<T, ID, FILT> = ApiItem.Delete.Action(
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
@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ICommonContainer<T, ID, FILT>.toIApiItem(
    apiItem: ApiItem<T, ID, FILT>
): IApiItem<T, ID, FILT> =
    when (apiItem) {
        is ApiItem.Upsert.Create.Query -> IApiItem.Upsert.Create.Query(
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Read -> IApiItem.Read(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Upsert.Update.Query -> IApiItem.Upsert.Update.Query(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Delete.Query -> IApiItem.Delete.Query(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Upsert.Create.Action -> IApiItem.Upsert.Create.Action(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Upsert.Update.Action -> IApiItem.Upsert.Update.Action(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter),
            serializedOrig = apiItem.orig?.let { Json.encodeToString(itemSerializer, it) }
        )

        is ApiItem.Delete.Action -> IApiItem.Delete.Action(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )
    }
