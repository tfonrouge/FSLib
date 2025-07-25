package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.types.ApplicationCall

/**
 * Sealed class representing various API items used to perform CRUD operations.
 *
 * @param T The type of the specific document being processed.
 * @param ID The type of the identifier for the specific document.
 * @param FILT The type of filter applied during API operations.
 */
sealed class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val apiFilter: FILT
    abstract val call: ApplicationCall?

    sealed class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        abstract val id: ID?
        override val callType: CallType = CallType.Query

        data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID? = null,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create
        }

        data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Read
        }

        data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update
        }

        data class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val id: ID,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Query<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
        }
    }

    sealed class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        abstract val item: T
        override val callType: CallType = CallType.Action
        abstract val asQuery: ApiItem<T, ID, FILT>

        data class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val item: T,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create
            override val asQuery: ApiItem<T, ID, FILT>
                get() = Query.Create(
                    id = item._id,
                    apiFilter = apiFilter,
                    call = call
                )
        }

        data class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val item: T,
            override val apiFilter: FILT,
            val orig: T?,
            override val call: ApplicationCall? = null,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update
            override val asQuery: ApiItem<T, ID, FILT>
                get() = Query.Update(
                    id = item._id,
                    apiFilter = apiFilter,
                    call = call
                )
        }

        class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            override val item: T,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Action<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Delete
            override val asQuery: ApiItem<T, ID, FILT>
                get() = Query.Delete(
                    id = item._id,
                    apiFilter = apiFilter,
                    call = call
                )
        }
    }
}
