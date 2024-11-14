package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

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
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Read<T, ID, FILT> = ApiItem.Read(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Upsert.Update.Query<T, ID, FILT> = ApiItem.Upsert.Update.Query(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Delete.Query<T, ID, FILT> = ApiItem.Delete.Query(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Upsert.Create.Action<T, ID, FILT> = ApiItem.Upsert.Create.Action(
        item = item,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
        orig: T?
    ): ApiItem.Upsert.Update.Action<T, ID, FILT> = ApiItem.Upsert.Update.Action(
        item = item,
        apiFilter = apiFilter,
        orig = orig
    )

    @Suppress("unused")
    fun apiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Delete.Action<T, ID, FILT> = ApiItem.Delete.Action(
        item = item,
        apiFilter = apiFilter
    )

    /* IApiItem */
    fun iApiItemQueryCreate(
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Upsert.Create.Query<T, ID, FILT> = IApiItem.Upsert.Create.Query(
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    fun iApiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Read<T, ID, FILT> = IApiItem.Read(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    fun iApiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Upsert.Update.Query<T, ID, FILT> = IApiItem.Upsert.Update.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    fun iApiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Delete.Query<T, ID, FILT> = IApiItem.Delete.Query(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    fun iApiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Upsert.Create.Action<T, ID, FILT> = IApiItem.Upsert.Create.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )


    fun iApiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance(),
        orig: T?
    ): IApiItem.Upsert.Update.Action<T, ID, FILT> = IApiItem.Upsert.Update.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter),
        serializedOrig = orig?.let { Json.encodeToString(itemSerializer, orig) }
    )

    fun iApiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Delete.Action<T, ID, FILT> = IApiItem.Delete.Action(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )
}

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