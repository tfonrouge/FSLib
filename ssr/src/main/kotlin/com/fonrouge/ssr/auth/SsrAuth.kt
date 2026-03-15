package com.fonrouge.ssr.auth

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.state.State
import com.fonrouge.fullStack.repository.IRepository
import io.ktor.server.application.*

/**
 * Interface for SSR permission checking.
 * Implementations determine whether a CRUD operation is allowed for the current request.
 */
interface SsrAuth {

    /**
     * Checks whether the given [crudTask] is permitted for the current [call].
     * Returns a [SimpleState] with [State.Ok] if allowed, [State.Error] if denied.
     */
    suspend fun checkPermission(
        call: ApplicationCall,
        crudTask: CrudTask,
        repository: IRepository<*, *, *, *>,
    ): SimpleState
}

/**
 * Default auth implementation that delegates to [IRepository.getCrudPermission].
 */
class RepositoryAuth : SsrAuth {

    override suspend fun checkPermission(
        call: ApplicationCall,
        crudTask: CrudTask,
        repository: IRepository<*, *, *, *>,
    ): SimpleState = repository.getCrudPermission(call, crudTask)
}

/**
 * Auth implementation that allows all operations.
 * Suitable for development and internal applications without role-based access.
 */
class AllowAllAuth : SsrAuth {

    override suspend fun checkPermission(
        call: ApplicationCall,
        crudTask: CrudTask,
        repository: IRepository<*, *, *, *>,
    ): SimpleState = SimpleState(State.Ok)
}
