package com.example.showcase

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState

/**
 * Contract interface for Task service operations.
 *
 * Shared between server (@RpcService interface extends this) and
 * Android client (proxy class implements this). Provides compile-time
 * validation that method signatures match on both sides.
 *
 * This interface has no Kilua RPC, Ktor, or other server-side dependencies.
 */
interface ITaskServiceContract {

    /**
     * Processes a paginated list request with filtering and sorting.
     */
    suspend fun apiList(apiList: ApiList<TaskFilter>): ListState<Task>

    /**
     * Processes a single-item CRUD request (create, read, update, delete).
     */
    suspend fun apiItem(iApiItem: IApiItem<Task, String, TaskFilter>): ItemState<Task>
}
