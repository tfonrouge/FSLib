package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc

sealed class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val apiFilter: FILT

    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Query
        abstract val id: ID?

        companion object {
            fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> build(
                commonContainer: ICommonContainer<T, ID, FILT>,
                crudTask: CrudTask,
                id: ID?,
                apiFilter: FILT = commonContainer.apiFilterInstance()
            ): Query<T, ID, FILT>? {
                return when (crudTask) {
                    CrudTask.Create -> Upsert.Create(
                        id = id,
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

        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> :
            Query<T, ID, FILT>() {
            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val id: ID? = null,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
            }

            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val id: ID,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
        }

        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID,
            override val apiFilter: FILT
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }

    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        override val callType: CallType = CallType.Action
        abstract val item: T

        sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> :
            Action<T, ID, FILT>() {

            data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val item: T,
                override val apiFilter: FILT
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Create
            }

            data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val item: T,
                override val apiFilter: FILT,
                val orig: T?
            ) : Upsert<T, ID, FILT>() {
                override val crudTask: CrudTask = CrudTask.Update
            }
        }

        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val item: T,
            override val apiFilter: FILT
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }
}
