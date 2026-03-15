package com.example.showcase

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import dev.kilua.rpc.annotations.RpcBindingRoute
import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service interface for Task operations.
 * Shared between JVM (implementation) and JS (client proxy).
 *
 * Extends [ITaskServiceContract] from showcase-lib so that Android
 * clients can implement the same contract for compile-time validation.
 *
 * Methods must be redeclared with `override` so that KSP generates
 * bind() calls and client proxy implementations for inherited methods.
 */
@RpcService
interface ITaskService : ITaskServiceContract {
    @RpcBindingRoute("ITaskService.apiList")
    override suspend fun apiList(apiList: ApiList<ApiFilter>): ListState<Task>
    @RpcBindingRoute("ITaskService.apiItem")
    override suspend fun apiItem(iApiItem: IApiItem<Task, String, ApiFilter>): ItemState<Task>
}
