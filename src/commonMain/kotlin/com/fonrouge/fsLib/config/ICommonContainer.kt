package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.apiServices.IApiService
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ICommonContainer<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter, AIS : IApiService>(
    val itemKClass: KClass<T>,
    val idSerializer: KSerializer<ID>,
    override val apiFilterSerializer: KSerializer<FILT>,
    val apiItemFun: (suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>) = {
        ItemState(
            isOk = false,
            msgError = "apiItemFun not defined for CommonContainer of ${this::class.simpleName}"
        )
    },
    // TODO: add here reference to ApiItem call function and remove it from ConfigViewItem
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
        id: ID? = null,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Query.Upsert.Create<T, ID, FILT> = ApiItem.Query.Upsert.Create(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Query.Read<T, ID, FILT> = ApiItem.Query.Read(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Query.Upsert.Update<T, ID, FILT> = ApiItem.Query.Upsert.Update(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Query.Delete<T, ID, FILT> = ApiItem.Query.Delete(
        id = id,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Action.Upsert.Create<T, ID, FILT> = ApiItem.Action.Upsert.Create(
        item = item,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Action.Upsert.Update<T, ID, FILT> = ApiItem.Action.Upsert.Update(
        item = item,
        apiFilter = apiFilter
    )

    @Suppress("unused")
    fun apiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): ApiItem.Action.Delete<T, ID, FILT> = ApiItem.Action.Delete(
        item = item,
        apiFilter = apiFilter
    )

    /* IApiItem */
    @Suppress("unused")
    fun iApiItemQueryCreate(
        id: ID? = null,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Query.Upsert.Create<T, ID, FILT> = IApiItem.Query.Upsert.Create(
        serializedId = id?.let { Json.encodeToString(idSerializer, it) },
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    fun iApiItemQueryRead(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Query.Read<T, ID, FILT> = IApiItem.Query.Read(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    fun iApiItemQueryUpdate(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Query.Upsert.Update<T, ID, FILT> = IApiItem.Query.Upsert.Update(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    fun iApiItemQueryDelete(
        id: ID,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Query.Delete<T, ID, FILT> = IApiItem.Query.Delete(
        serializedId = Json.encodeToString(idSerializer, id),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    fun iApiItemActionCreate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Action.Upsert.Create<T, ID, FILT> = IApiItem.Action.Upsert.Create(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )


    @Suppress("unused")
    fun iApiItemActionUpdate(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Action.Upsert.Update<T, ID, FILT> = IApiItem.Action.Upsert.Update(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    fun iApiItemActionDelete(
        item: T,
        apiFilter: FILT = apiFilterInstance()
    ): IApiItem.Action.Delete<T, ID, FILT> = IApiItem.Action.Delete(
        serializedItem = Json.encodeToString(itemSerializer, item),
        serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    )

    @Suppress("unused")
    open fun validateItem(item: T, apiFilter: FILT = apiFilterInstance()): ItemState<T> {
        return ItemState(isOk = true)
    }
}

fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ICommonContainer<T, ID, FILT, *>.toIApiItem(apiItem: ApiItem<T, ID, FILT>): IApiItem<T, ID, FILT> =
    when (apiItem) {
        is ApiItem.Query.Upsert.Create -> IApiItem.Query.Upsert.Create(
            serializedId = apiItem.id?.let { Json.encodeToString(idSerializer, it) },
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Query.Read -> IApiItem.Query.Read(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Query.Upsert.Update -> IApiItem.Query.Upsert.Update(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Query.Delete -> IApiItem.Query.Delete(
            serializedId = Json.encodeToString(idSerializer, apiItem.id),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Action.Upsert.Create -> IApiItem.Action.Upsert.Create(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Action.Upsert.Update -> IApiItem.Action.Upsert.Update(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )

        is ApiItem.Action.Delete -> IApiItem.Action.Delete(
            serializedItem = Json.encodeToString(itemSerializer, apiItem.item),
            serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiItem.apiFilter)
        )
    }