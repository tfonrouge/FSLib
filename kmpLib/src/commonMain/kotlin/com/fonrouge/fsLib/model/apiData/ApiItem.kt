package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.base.IUser

/**
 * A sealed class representing different types of API operations.
 *
 * @param T The type of the document extending [BaseDoc].
 * @param ID The type of the identifier for the documents.
 * @param FILT The type of filter extending [IApiFilter].
 */
sealed class ApiItem<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> {
    abstract val callType: CallType
    abstract val crudTask: CrudTask
    abstract val apiFilter: FILT
    abstract val iUser: IUser<*>?

    sealed class Upsert<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        sealed class Create<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Create

            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                override val apiFilter: FILT,
                override val iUser: IUser<*>? = null
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val item: T,
                override val apiFilter: FILT,
                override val iUser: IUser<*>? = null
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
            }
        }

        sealed class Update<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : Upsert<T, ID, FILT>() {
            override val crudTask: CrudTask = CrudTask.Update

            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val id: ID,
                override val apiFilter: FILT,
                override val iUser: IUser<*>? = null
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val item: T,
                override val apiFilter: FILT,
                val orig: T?,
                override val iUser: IUser<*>? = null
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Action
            }
        }
    }

    data class Read<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
        val id: ID,
        override val apiFilter: FILT,
        override val iUser: IUser<*>? = null
    ) : ApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Read
        override val callType: CallType = CallType.Query
    }

    sealed class Delete<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> : ApiItem<T, ID, FILT>() {
        override val crudTask: CrudTask = CrudTask.Delete

        data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val id: ID,
            override val apiFilter: FILT,
            override val iUser: IUser<*>? = null
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Query
        }

        data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val item: T,
            override val apiFilter: FILT,
            override val iUser: IUser<*>? = null
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Action
        }
    }
}
