package com.fonrouge.fullStack.repository

import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.SimpleState

/**
 * Backend-agnostic interface for change log recording.
 *
 * Implementations track mutations (create, update, delete) made to entities,
 * storing before/after snapshots for audit purposes. The MongoDB implementation
 * ([com.fonrouge.fullStack.mongoDb.IChangeLogColl]) and potential SQL implementations
 * both fulfill this contract.
 */
interface IChangeLogRepository {

    /**
     * Builds and persists a change log entry for a CRUD action.
     *
     * @param cc The common container managing the entity's metadata.
     * @param apiItem The action that was performed (create, update, or delete).
     * @param orig The original item before the mutation, or null for create/delete actions.
     * @return A [SimpleState] indicating success or failure of the log entry creation.
     */
    suspend fun <CC : ICommonContainer<T, ID, *>, T : BaseDoc<ID>, ID : Any> buildChangeLog(
        cc: CC,
        apiItem: ApiItem.Action<T, ID, *>,
        orig: T?,
    ): SimpleState
}
