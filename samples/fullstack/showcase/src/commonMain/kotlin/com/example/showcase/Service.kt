package com.example.showcase

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service interface for Task operations.
 * Shared between JVM (implementation) and JS (client proxy).
 */
@RpcService
interface ITaskService {

    /**
     * Processes a paginated list request with filtering and sorting.
     */
    suspend fun apiList(apiList: ApiList<TaskFilter>): ListState<Task>

    /**
     * Processes a single-item CRUD request (create, read, update, delete).
     */
    suspend fun apiItem(iApiItem: IApiItem<Task, String, TaskFilter>): ItemState<Task>
}
