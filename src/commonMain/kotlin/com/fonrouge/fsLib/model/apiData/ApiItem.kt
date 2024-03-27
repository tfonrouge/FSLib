package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.json.Json

sealed class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val apiFilter: FILT
    abstract fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT>

    companion object {
        fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> build(
            commonContainer: ICommonContainer<T, ID, FILT>,
            id: ID?,
            item: T?,
            callType: CallType = CallType.Query,
            crudTask: CrudTask,
            apiFilter: FILT = commonContainer.apiFilterInstance(),
        ): ApiItem<T, ID, FILT>? {
            return when (callType) {
                CallType.Query -> when (crudTask) {
                    CrudTask.Create -> Query.Upsert.Create(
                        apiFilter = apiFilter
                    )

                    CrudTask.Read -> id?.let {
                        Query.Read(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Update -> id?.let {
                        Query.Upsert.Update(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Query.Delete(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }
                }

                CallType.Action -> when (crudTask) {
                    CrudTask.Create -> item?.let {
                        Action.Upsert.Create(
                            item = it,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Read -> null
                    CrudTask.Update -> item?.let {
                        Action.Upsert.Update(
                            item = it,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Action.Delete(
                            id = it,
                            apiFilter = apiFilter
                        )
                    }
                }
            }
        }
    }

    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Query
        abstract val id: ID?

        companion object {
            fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> build(
                commonContainer: ICommonContainer<T, ID, FILT>,
                crudTask: CrudTask,
                id: ID?,
                apiFilter: FILT = commonContainer.apiFilterInstance()
            ): Query<T, ID, FILT>? {
                return when (crudTask) {
                    CrudTask.Create -> Upsert.Create(
                        apiFilter = apiFilter
                    )

                    CrudTask.Read -> id?.let {
                        Read(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Update -> id?.let {
                        Upsert.Update(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Delete(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }
                }
            }
        }

        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Query<T, ID, FILT>() {
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
                override val id: ID? = null
                override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                    return IApiItem.Query.Upsert.Create(
                        serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                    )
                }
            }

            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val id: ID,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
                override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                    return IApiItem.Query.Upsert.Update(
                        serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                        serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                    )
                }
            }
        }

        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val id: ID,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
            override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                return IApiItem.Query.Read(
                    serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                    serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                )
            }
        }

        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            override val id: ID,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
            override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                return IApiItem.Query.Delete(
                    serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                    serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                )
            }
        }
    }

    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Action

        companion object {
            fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> build(
                commonContainer: ICommonContainer<T, ID, FILT>,
                crudTask: CrudTask,
                id: ID? = null,
                item: T? = null,
                apiFilter: FILT = commonContainer.apiFilterInstance()
            ): Action<T, ID, FILT>? {
                return when (crudTask) {
                    CrudTask.Create -> item?.let {
                        Upsert.Create(
                            item = item,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Read -> null
                    CrudTask.Update -> item?.let {
                        Upsert.Update(
                            item = item,
                            apiFilter = apiFilter
                        )
                    }

                    CrudTask.Delete -> id?.let {
                        Delete(
                            id = id,
                            apiFilter = apiFilter
                        )
                    }
                }
            }
        }

        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> : Action<T, ID, FILT>() {
            abstract val item: T

            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val item: T,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
                override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                    return IApiItem.Action.Upsert.Create(
                        serializedItem = Json.encodeToString(commonContainer.itemSerializer, item),
                        serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                    )
                }
            }

            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
                override val item: T,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
                override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                    return IApiItem.Action.Upsert.Update(
                        serializedItem = Json.encodeToString(commonContainer.itemSerializer, item),
                        serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                    )
                }
            }
        }

        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
            val id: ID,
            override val apiFilter: FILT
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
            override fun asIApiItem(commonContainer: ICommonContainer<T, ID, FILT>): IApiItem<T, ID, FILT> {
                return IApiItem.Action.Delete(
                    serializedId = Json.encodeToString(commonContainer.idSerializer, id),
                    serializedApiFilter = Json.encodeToString(commonContainer.apiFilterSerializer, apiFilter)
                )
            }
        }
    }
}
