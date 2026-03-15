package com.fonrouge.fullStack.services

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.api.IApiItem
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import com.fonrouge.base.state.ListState
import com.fonrouge.fullStack.repository.IRepository
import io.ktor.server.application.*

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
 * **Permission checks:** By default, [currentCall] returns `null`, which means
 * role-based permission checks in [IRepository.apiItemProcess] and
 * [IRepository.apiListProcess] are skipped. Override [currentCall] in subclasses
 * running inside Ktor to supply the request context:
 *
 * ```kotlin
 * class TaskService(repo: Coll<Task, OId<Task>, ApiFilter, UserId>) :
 *     StandardCrudService<Task, OId<Task>, ApiFilter>(repo), ITaskService {
 *     override fun currentCall(): ApplicationCall? = /* Ktor call from service scope */
 * }
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
     * Returns the current [ApplicationCall] for permission checks, or `null` to skip them.
     *
     * Override this in services that run inside a Ktor request pipeline to enable
     * role-based access control via [IRepository.apiItemProcess] and [IRepository.apiListProcess].
     *
     * @return The current Ktor [ApplicationCall], or `null` if unavailable.
     */
    protected open fun currentCall(): ApplicationCall? = null

    /**
     * Processes a paginated list request by delegating to [IRepository.apiListProcess].
     *
     * @param apiList The list request parameters (pagination, filters, sorters).
     * @return [ListState] with the result data and pagination info.
     */
    open suspend fun apiList(apiList: ApiList<FILT>): ListState<T> =
        repository.apiListProcess(call = currentCall(), apiList = apiList)

    /**
     * Processes a single-item CRUD request by delegating to [IRepository.apiItemProcess].
     *
     * @param iApiItem The serialized API item to process.
     * @return [ItemState] with the result of the operation.
     */
    open suspend fun apiItem(iApiItem: IApiItem<T, ID, FILT>): ItemState<T> =
        repository.apiItemProcess(call = currentCall(), iApiItem = iApiItem)
}
