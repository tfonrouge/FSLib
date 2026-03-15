package com.example.showcase

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.memoryDb.InMemoryRepository

/**
 * Server-side implementation of [ITaskService].
 * Delegates all operations to the [InMemoryRepository].
 */
class TaskService(
    private val repo: InMemoryRepository<Task, String, ApiFilter, String>,
) : ITaskService {

    override suspend fun apiList(apiList: ApiList<ApiFilter>): ListState<Task> =
        repo.apiListProcess(apiList = apiList)

    override suspend fun apiItem(iApiItem: IApiItem<Task, String, ApiFilter>): ItemState<Task> =
        repo.apiItemProcess(call = null, iApiItem = iApiItem)
}
