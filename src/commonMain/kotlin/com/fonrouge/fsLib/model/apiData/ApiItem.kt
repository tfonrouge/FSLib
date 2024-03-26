package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/*
@Suppress("unused")
@Serializable
data class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val id: ID? = null,
    val item: T? = null,
    val callType: CallType = CallType.Query,
    val crudTask: CrudTask = CrudTask.Read,
    val apiFilter: FILT,
) {
    @Serializable
    enum class CallType {
        Query,
        Action
    }
}
*/

@Serializable
sealed class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val apiFilter: FILT

    companion object {
        fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> build(
            commonContainer: ICommonContainer<T, ID, FILT>,
            id: ID?,
            item: T?,
            callType: CallType = CallType.Query,
            crudTask: CrudTask,
            apiFilter: FILT,
        ): ApiItem<T, ID, FILT>? {
            return when (callType) {
                CallType.Query -> when (crudTask) {
                    CrudTask.Create -> Query.Upsert.Create(
                        apiFilter = apiFilter
                    )

                    CrudTask.Read -> id?.let {
                        Query.Read(
                            serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Update -> id?.let {
                        Query.Upsert.Update(
                            serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Query.Delete(
                            serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                            apiFilter = apiFilter
                        )
                    }
                }

                CallType.Action -> when (crudTask) {
                    CrudTask.Create -> item?.let {
                        Action.Upsert.Create(
                            serializedItem = Json.encodeToString(commonContainer.itemSerializer, it),
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Read -> null
                    CrudTask.Update -> item?.let {
                        Action.Upsert.Update(
                            serializedItem = Json.encodeToString(commonContainer.itemSerializer, it),
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Action.Delete(
                            serializedId = Json.encodeToString(commonContainer.idSerializer, it),
                            apiFilter = apiFilter
                        )
                    }
                }
            }
        }
    }

    @Serializable
    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Query
        abstract val serializedId: String?

        companion object {
            fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> build(
                commonContainer: ICommonContainer<T, ID, FILT>,
                crudTask: CrudTask,
                id: ID?,
                apiFilter: FILT
            ): Query<T, ID, FILT>? {
                return when (crudTask) {
                    CrudTask.Create -> Upsert.Create(apiFilter = apiFilter)
                    CrudTask.Read -> null
                    CrudTask.Update -> id?.let {
                        Upsert.Update(
                            serializedId = Json.encodeToString(
                                commonContainer.idSerializer,
                                id
                            ),
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Delete(
                            serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                            apiFilter = apiFilter
                        )
                    }
                }
            }
        }

        @Serializable
        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Query<T, ID, FILT>() {
            @Serializable
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
                override val serializedId: String? = null
            }

            @Serializable
            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedId: String,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        @Serializable
        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val serializedId: String,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val serializedId: String,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }

    @Serializable
    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Action

        @Serializable
        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Action<T, ID, FILT>() {
            abstract val serializedItem: String

            @Serializable
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedItem: String,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
            }

            @Serializable
            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val serializedItem: String,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        @Serializable
        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            val serializedId: String,
            override val apiFilter: FILT
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }

    @Serializable
    enum class CallType {
        Query,
        Action
    }
}

fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ApiItem.Query<T, ID, FILT>.id(commonContainer: ICommonContainer<T, ID, FILT>): ID? {
    return serializedId?.let { Json.decodeFromString(commonContainer.idSerializer, it) }
}

fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ApiItem.Action.Delete<T, ID, FILT>.id(commonContainer: ICommonContainer<T, ID, FILT>): ID {
    return Json.decodeFromString(commonContainer.idSerializer, serializedId)
}

fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ApiItem.Action.Upsert<T, ID, FILT>.item(commonContainer: ICommonContainer<T, ID, FILT>): T {
    return Json.decodeFromString(commonContainer.itemSerializer, serializedItem)
}