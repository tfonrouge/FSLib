package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IRoleInGroup
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.base.IUserGroup
import com.fonrouge.fsLib.types.OId
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
) : Coll<ICommonContainer<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>, UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UG>.indexes() {
        ensureUniqueIndex(
            IUserGroup<U, UID, GOU, *>::userId,
            IUserGroup<U, UID, GOU, *>::groupOfUserId
        )
    }
}
