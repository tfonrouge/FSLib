package com.fonrouge.fullStack.services

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.repository.IRepository

/**
 * Base class for service implementations that delegate standard CRUD operations
 * to an [IRepository].
 *
 * Eliminates the boilerplate of writing `apiList` / `apiItem` methods that simply
 * forward to the repository. Concrete services only need to extend this class
 * and implement the RPC service interface:
 *
 * ```kotlin
 * class TaskService(repo: InMemoryRepository<Task, String, ApiFilter, String>) :
 *     StandardCrudService<Task, String, ApiFilter>(repo), ITaskService
 * ```
 *
 * @param T The entity type, must implement [BaseDoc].
 * @param ID The identifier type.
 * @param FILT The filter type, must implement [IApiFilter].
 * @property repository The backend repository handling persistence.
 */
abstract class StandardCrudService<T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    protected val repository: IRepository<T, ID, FILT, *>,
) {
    /**
     * Processes a paginated list request by delegating to [IRepository.apiListProcess].
     *
     * @param apiList The list request parameters (pagination, filters, sorters).
     * @return [ListState] with the result data and pagination info.
     */
    suspend fun apiList(apiList: ApiList<FILT>): ListState<T> =
        repository.apiListProcess(apiList = apiList)

    /**
     * Processes a single-item CRUD request by delegating to [IRepository.apiItemProcess].
     *
     * @param iApiItem The serialized API item to process.
     * @return [ItemState] with the result of the operation.
     */
    suspend fun apiItem(iApiItem: IApiItem<T, ID, FILT>): ItemState<T> =
        repository.apiItemProcess(call = null, iApiItem = iApiItem)
}
