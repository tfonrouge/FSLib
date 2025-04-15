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

            /**
             * Represents a specialized query operation within a Create context for handling API requests.
             *
             * @param T The type of the document being queried, extending the BaseDoc interface.
             * @param ID The type of the identifier for the document.
             * @param FILT The type of the filter applied to the query, extending the IApiFilter interface.
             * @property id An optional identifier for the specific document being queried.
             * @property apiFilter The filter criteria for refining the query process.
             * @property call The application call instance associated with the query.
             * @constructor Initializes a new Query object.
             *
             * This class is a subtype of Create and specializes in query-like operations, providing
             * refinements for the CRUD Create task. The default call type for this operation is `CallType.Query`.
             */
            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val id: ID? = null,
                override val apiFilter: FILT,
                override val call: ApplicationCall? = null,
            ) : Create<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            /**
             * Represents a specialized action operation within a Create context for handling API requests.
             *
             * @param T The type of the document being processed, which extends the BaseDoc interface.
             * @param ID The type of the identifier for the document.
             * @param FILT The type of the filter being applied, which extends the IApiFilter interface.
             * @property item The specific item to be processed within the action.
             * @property apiFilter The filter criteria applied during the action process.
             * @property call The application call context associated with the action.
             *
             * This class is a subtype of Create and is specifically designed for handling action-based tasks.
             * The default call type for this operation is `CallType.Action`.
             */
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

            /**
             * Defines a query operation to retrieve or filter data within the application.
             *
             * @param T The type of the document being queried, extending [BaseDoc].
             * @param ID The type of the unique identifier for the document.
             * @param FILT The type representing the API filter applied to the query, extending [IApiFilter].
             * @property id The unique identifier for the document being queried.
             * @property apiFilter The filter criteria applied to the query.
             * @property call The call context associated with the query, defaulting to null.
             * @property callType Indicates that this operation is of type [CallType.Query].
             */
            data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
                val id: ID,
                override val apiFilter: FILT,
                override val call: ApplicationCall? = null,
            ) : Update<T, ID, FILT>() {
                override val callType: CallType = CallType.Query
            }

            /**
             * Represents an action operation within a CRUD context.
             *
             * This class is part of the `Update` hierarchy and is used to define actions
             * involving modification of data in the system, such as performing updates
             * or specific operations on a document.
             *
             * @param T The type of the document associated with the action, which extends [BaseDoc].
             * @param ID The type of the unique identifier for the document.
             * @param FILT The type of the API filter applied to the action, extending [IApiFilter].
             * @property item The document that is the subject of the action.
             * @property apiFilter The filter criteria applied to narrow down the scope of the action.
             * @property orig The original document, which can be used for comparison or reference. It may be null.
             * @property call The call context associated with the action, which is optional and defaults to null.
             * @property callType Indicates that this operation is of type [CallType.Action].
             */
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

    /**
     * Represents a Read operation in the API context.
     *
     * This class is a concrete implementation of the `ApiItem` and is responsible
     * for handling data retrieval tasks defined by the CRUD task. The Read task
     * is associated with querying operations which are represented by the `CallType.Query`.
     *
     * @param T The type of the resource being retrieved, which must extend the `BaseDoc` interface.
     * @param ID The type of the identifier for the resource.
     * @param FILT The type of the API filter applied to constrain the read operation, which must extend `IApiFilter`.
     * @property id The identifier for the resource to be retrieved.
     * @property apiFilter Provides filtering options for the read operation.
     * @property call The application call associated with the task, if applicable.
     */
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

        /**
         * Represents a query operation in the context of deletion tasks.
         *
         * This class is part of the hierarchy of API actions and is specifically used to define
         * a query-oriented delete operation. The `Query` class is parameterized with the type of
         * the document, its identifier type, and an API filter type. It inherits behavior from the
         * `Delete` sealed class.
         *
         * @param id The identifier of the document being targeted in the query.
         * @param apiFilter The filter criteria associated with the query.
         * @param call Optional application call context for the query operation.
         *
         * @property callType The type of API call, which is specific to query operations in this case.
         */
        data class Query<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val id: ID,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Query
        }

        /**
         * Represents an action type in the context of API deletion operations.
         *
         * The `Action` class is part of the `Delete` hierarchy and is used to define
         * operations that are intended to perform specific actions on a given item
         * using the associated filter and optional application call context.
         *
         * @param item The document or entity on which the action is being performed.
         * @param apiFilter The filter criteria used for the action.
         * @param call An optional application call context associated with the action.
         *
         * @property callType Defines the type of API call as an action.
         */
        data class Action<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
            val item: T,
            override val apiFilter: FILT,
            override val call: ApplicationCall? = null,
        ) : Delete<T, ID, FILT>() {
            override val callType: CallType = CallType.Action
        }
    }
}
