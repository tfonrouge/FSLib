package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IGroupOfUser
import com.fonrouge.base.model.IRoleInGroup
import com.fonrouge.base.model.IUser
import com.fonrouge.base.model.IUserGroup
import com.fonrouge.base.types.OId
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Abstract collection class for managing user groups.
 *
 * @param UG The type of user group extending from `IUserGroup`.
 * @param U The type of user extending from `IUser`.
 * @param UID The type of the user identifier.
 * @param GOU The type of group of user extending from `IGroupOfUser`.
 * @param GR The type of role in group extending from `IRoleInGroup`.
 * @param FILT The type of API filter extending from `IApiFilter`.
 * @param commonContainer The common container instance for this collection.
 */
abstract class IUserGroupColl<UG : IUserGroup<U, UID, GOU, GR>, U : IUser<out UID>, UID : Any, GOU : IGroupOfUser<*>, GR : IRoleInGroup<*, GOU>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>,
) : Coll<ICommonContainer<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>, UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT, UID>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UG>.indexes() {
        ensureUniqueIndex(
            IUserGroup<U, UID, GOU, *>::userId,
            IUserGroup<U, UID, GOU, *>::groupOfUserId
        )
    }
}
