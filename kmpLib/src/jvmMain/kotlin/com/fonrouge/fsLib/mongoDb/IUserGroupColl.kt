package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IGroupRole
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.base.IUserGroup
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class IUserGroupColl<UG : IUserGroup<U, UID, GOU, GR>, U : IUser<out UID>, UID : Any, GOU : IGroupOfUser<*>, GR : IGroupRole<*, GOU>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>
) : Coll<ICommonContainer<UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>, UG, OId<IUserGroup<U, UID, GOU, GR>>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UG>.ensureIndexes() {
        ensureUniqueIndex(
            IUserGroup<U, UID, GOU, *>::userId,
            IUserGroup<U, UID, GOU, *>::groupOfUserId
        )
    }
}
