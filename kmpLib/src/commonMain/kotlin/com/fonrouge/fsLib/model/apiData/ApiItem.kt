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

    sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        sealed class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create

            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val apiFilter: FILT,
                override val call: ApplicationCall? = null,
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val item: T,
                override val apiFilter: FILT,
                override val call: ApplicationCall? = null,
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
            }
        }

        sealed class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update

            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val id: ID,
                override val apiFilter: FILT,
                override val call: ApplicationCall? = null,
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val item: T,
                override val apiFilter: FILT,
                val orig: T?,
                override val call: ApplicationCall? = null,
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
            }
        }
    }

    data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
        val id: ID,
        override val apiFilter: FILT,
        override val call: ApplicationCall? = null,
    ) : ApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Read
        override val callType: CallType = CallType.Query
    }

    sealed class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Delete

        data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val id: ID,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Query
        }

        data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val item: T,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Action
        }
    }
}
