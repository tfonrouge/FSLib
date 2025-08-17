package com.fonrouge.backendLib.mongoDb

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.model.IGroupOfUser
import com.fonrouge.base.model.IRoleInGroup
import com.fonrouge.base.types.OId
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Abstract class representing a collection that manages roles within groups.
 *
 * @param GR The type of role in group.
 * @param T The type parameter for the identifier.
 * @param GOU The type of group of user.
 * @param FILT The type of API filter.
 * @param commonContainer The common container to be used in the collection.
 */
abstract class IRoleInGroupColl<GR : IRoleInGroup<T, GOU>, T : Any, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<GR, OId<T>, FILT>,
) : Coll<ICommonContainer<GR, OId<T>, FILT>, GR, OId<T>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<GR>.indexes() {
        ensureUniqueIndex(IRoleInGroup<T, GOU>::groupOfUserId, IRoleInGroup<T, GOU>::appRoleId)
    }
}
