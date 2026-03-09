package com.fonrouge.fullStack.repository

import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.SimpleState

/**
 * Backend-agnostic interface for change log recording.
 *
 * Implementations track mutations (create, update, delete) made to entities,
 * storing before/after snapshots for audit purposes. Database-specific implementations
 * (e.g. MongoDB's `IChangeLogColl`, or a future SQL variant) fulfill this contract.
 *
 * @see IRepository
 */
interface IChangeLogRepository {

    /**
     * Builds and persists a change log entry for a CRUD action.
     *
     * @param CC The [ICommonContainer] subtype that manages the entity.
     * @param T The document type being tracked.
     * @param ID The document's primary key type.
     * @param cc The common container managing the entity's metadata and repository access.
     * @param apiItem The CRUD action that was performed (create, update, or delete).
     * @param orig The original document state before the mutation, or `null` for create actions.
     * @return A [SimpleState] indicating success or failure of the log entry creation.
     */
    suspend fun <CC : ICommonContainer<T, ID, *>, T : BaseDoc<ID>, ID : Any> buildChangeLog(
        cc: CC,
        apiItem: ApiItem.Action<T, ID, *>,
        orig: T?,
    ): SimpleState
}
