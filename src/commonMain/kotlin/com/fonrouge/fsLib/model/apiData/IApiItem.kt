package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("#type")
sealed class IApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val serializedApiFilter: String

    open fun asApiItem(cc: ICommonContainer<T, ID, FILT>): ApiItem<T, ID, FILT> {
        return when (this) {
            is Query -> when (this) {
                is Query.Upsert.Create -> ApiItem.Query.Upsert.Create(
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )

                is Query.Read -> ApiItem.Query.Read(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )

                is Query.Upsert.Update -> ApiItem.Query.Upsert.Update(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )

                is Query.Delete -> ApiItem.Query.Delete(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )
            }

            is Action -> when (this) {
                is Action.Upsert.Create -> ApiItem.Action.Upsert.Create(
                    item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )

                is Action.Upsert.Update -> ApiItem.Action.Upsert.Update(
                    item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )

                is Action.Delete -> ApiItem.Action.Delete(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                )
            }
        }
    }

    @Serializable
    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : IApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Query
        abstract val serializedId: String?

        @Serializable
        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Query<T, ID, FILT>() {
            @Serializable
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedApiFilter: String
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
                override val serializedId: String? = null
                override fun asApiItem(cc: ICommonContainer<T, ID, FILT>): ApiItem<T, ID, FILT> {
                    return ApiItem.Query.Upsert.Create(
                        apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter)
                    )
                }
            }

            @Serializable
            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedId: String,
                override val serializedApiFilter: String
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        @Serializable
        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val serializedId: String,
            override val serializedApiFilter: String
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val serializedId: String,
            override val serializedApiFilter: String
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }

    @Serializable
    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : IApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Action

        @Serializable
        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Action<T, ID, FILT>() {
            abstract val serializedItem: String

            @Serializable
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedItem: String,
                override val serializedApiFilter: String
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
            }

            @Serializable
            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedItem: String,
                override val serializedApiFilter: String
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            val serializedId: String,
            override val serializedApiFilter: String
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }
}
