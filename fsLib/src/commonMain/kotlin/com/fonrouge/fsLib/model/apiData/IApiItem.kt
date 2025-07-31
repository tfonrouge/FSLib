package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.types.ApplicationCall
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * A sealed class representing an API item with generic parameters.
 *
 * @param T The type of the Base Document.
 * @param ID The type of the Identifier for the Base Document.
 * @param FILT The type of the API Filter.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("#type")
sealed class IApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val serializedApiFilter: String
    abstract fun asApiItem(
        cc: ICommonContainer<T, ID, FILT>,
        call: ApplicationCall?,
    ): ApiItem<T, ID, FILT>

    @Serializable
    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : IApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Query

        @Serializable
        data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedId: String?,
            override val serializedApiFilter: String,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Query.Create<T, ID, FILT> = ApiItem.Query.Create(
                id = serializedId?.let { Json.decodeFromString(cc.idSerializer, it) },
                apiFilter = Json.decodeFromString(
                    cc.apiFilterSerializer,
                    serializedApiFilter
                ),
                call = call,
            )
        }

        @Serializable
        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedId: String,
            override val serializedApiFilter: String,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Query.Read<T, ID, FILT> = ApiItem.Query.Read(
                id = Json.decodeFromString(cc.idSerializer, serializedId),
                apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                call = call,
            )
        }

        @Serializable
        data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedId: String,
            override val serializedApiFilter: String,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Query.Update<T, ID, FILT> = ApiItem.Query.Update(
                id = Json.decodeFromString(cc.idSerializer, serializedId),
                apiFilter = Json.decodeFromString(
                    cc.apiFilterSerializer,
                    serializedApiFilter
                ),
                call = call,
            )
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedId: String,
            override val serializedApiFilter: String,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Query.Delete<T, ID, FILT> {
                return ApiItem.Query.Delete(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                    call = call,
                )
            }
        }
    }

    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : IApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Action

        @Serializable
        data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedItem: String,
            override val serializedApiFilter: String,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Action.Create<T, ID, FILT> = ApiItem.Action.Create(
                item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                apiFilter = Json.decodeFromString(
                    cc.apiFilterSerializer,
                    serializedApiFilter
                ),
                call = call,
            )
        }

        @Serializable
        data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedItem: String,
            override val serializedApiFilter: String,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Action.Update<T, ID, FILT> = ApiItem.Action.Update(
                item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                apiFilter = Json.decodeFromString(
                    cc.apiFilterSerializer,
                    serializedApiFilter
                ),
                call = call,
            )
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedItem: String,
            override val serializedApiFilter: String,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem.Action.Delete<T, ID, FILT> {
                return ApiItem.Action.Delete(
                    item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                    call = call,
                )
            }
        }
    }
}
