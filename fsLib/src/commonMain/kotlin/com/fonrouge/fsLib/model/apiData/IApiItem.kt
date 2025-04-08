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
    sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : IApiItem<T, ID, FILT>() {
        @Serializable
        sealed class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> :
            Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create

            @Serializable
            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val serializedId: String?,
                override val serializedApiFilter: String
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
                override fun asApiItem(
                    cc: ICommonContainer<T, ID, FILT>,
                    call: ApplicationCall?,
                ): ApiItem<T, ID, FILT> {
                    return ApiItem.Upsert.Create.Query(
                        id = serializedId?.let { Json.decodeFromString(cc.idSerializer, it) },
                        apiFilter = Json.decodeFromString(
                            cc.apiFilterSerializer,
                            serializedApiFilter
                        ),
                        call = call,
                    )
                }
            }

            @Serializable
            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val serializedItem: String,
                override val serializedApiFilter: String
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
                override fun asApiItem(
                    cc: ICommonContainer<T, ID, FILT>,
                    call: ApplicationCall?,
                ): ApiItem<T, ID, FILT> {
                    return ApiItem.Upsert.Create.Action(
                        item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                        apiFilter = Json.decodeFromString(
                            cc.apiFilterSerializer,
                            serializedApiFilter
                        ),
                        call = call,
                    )
                }
            }
        }

        @Serializable
        sealed class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> :
            Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update

            @Serializable
            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val serializedId: String,
                override val serializedApiFilter: String,
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
                override fun asApiItem(
                    cc: ICommonContainer<T, ID, FILT>,
                    call: ApplicationCall?,
                ): ApiItem<T, ID, FILT> {
                    return ApiItem.Upsert.Update.Query(
                        id = Json.decodeFromString(cc.idSerializer, serializedId),
                        apiFilter = Json.decodeFromString(
                            cc.apiFilterSerializer,
                            serializedApiFilter
                        ),
                        call = call,
                    )
                }
            }

            @Serializable
            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val serializedItem: String,
                override val serializedApiFilter: String,
                val serializedOrig: String?
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
                override fun asApiItem(
                    cc: ICommonContainer<T, ID, FILT>,
                    call: ApplicationCall?,
                ): ApiItem<T, ID, FILT> {
                    return ApiItem.Upsert.Update.Action(
                        item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                        apiFilter = Json.decodeFromString(
                            cc.apiFilterSerializer,
                            serializedApiFilter
                        ),
                        orig = serializedOrig?.let {
                            Json.decodeFromString(
                                cc.itemSerializer,
                                serializedOrig
                            )
                        },
                        call = call,
                    )
                }
            }
        }
    }

    @Serializable
    data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
        val serializedId: String,
        override val serializedApiFilter: String
    ) : IApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Read
        override val callType: CallType = CallType.Query
        override fun asApiItem(
            cc: ICommonContainer<T, ID, FILT>,
            call: ApplicationCall?,
        ): ApiItem<T, ID, FILT> {
            return ApiItem.Read(
                id = Json.decodeFromString(cc.idSerializer, serializedId),
                apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                call = call,
            )
        }
    }

    @Serializable
    sealed class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : IApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Delete

        @Serializable
        data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedId: String,
            override val serializedApiFilter: String
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Query
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem<T, ID, FILT> {
                return ApiItem.Delete.Query(
                    id = Json.decodeFromString(cc.idSerializer, serializedId),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                    call = call,
                )
            }
        }

        @Serializable
        data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val serializedItem: String,
            override val serializedApiFilter: String
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Action
            override fun asApiItem(
                cc: ICommonContainer<T, ID, FILT>,
                call: ApplicationCall?,
            ): ApiItem<T, ID, FILT> {
                return ApiItem.Delete.Action(
                    item = Json.decodeFromString(cc.itemSerializer, serializedItem),
                    apiFilter = Json.decodeFromString(cc.apiFilterSerializer, serializedApiFilter),
                    call = call,
                )
            }
        }
    }
}
