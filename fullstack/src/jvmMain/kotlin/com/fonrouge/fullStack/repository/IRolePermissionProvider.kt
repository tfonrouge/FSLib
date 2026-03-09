package com.fonrouge.fullStack.repository

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.state.SimpleState
import io.ktor.server.application.*

/**
 * Backend-agnostic interface for role-based CRUD permission checking.
 *
 * This abstraction decouples the permission system from any specific database engine.
 * The MongoDB module registers its implementation (backed by `IRoleInUserColl`) at startup;
 * other engines (e.g., SQL) consume it through [PermissionRegistry] without importing
 * MongoDB-specific types.
 */
interface IRolePermissionProvider {

    /**
     * Checks whether the current user has permission for the specified CRUD task
     * on the given entity container.
     *
     * @param commonContainer The container providing entity metadata.
     * @param call The Ktor request context containing session/user information.
     * @param crudTask The CRUD operation to check permission for.
     * @return [SimpleState] indicating whether the operation is permitted.
     */
    suspend fun getCrudPermission(
        commonContainer: ICommonContainer<*, *, *>,
        call: ApplicationCall,
        crudTask: CrudTask,
    ): SimpleState
}

/**
 * Global registry for the role-based permission provider.
 *
 * The MongoDB module registers its [IRolePermissionProvider] implementation here
 * during initialization. Other repository implementations (e.g., [SqlRepository])
 * query this registry to check CRUD permissions without depending on MongoDB types.
 *
 * When no provider is registered, permission checks default to "allowed".
 */
object PermissionRegistry {

    /**
     * The currently registered permission provider, or null if none is registered.
     * When null, all permission checks are implicitly allowed.
     */
    var rolePermissionProvider: IRolePermissionProvider? = null
}
