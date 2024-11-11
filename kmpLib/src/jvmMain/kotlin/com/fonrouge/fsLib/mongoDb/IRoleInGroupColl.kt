package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IRoleInGroup
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class IRoleInGroupColl<GR : IRoleInGroup<T, GOU>, T : Any, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<GR, OId<T>, FILT>
) : Coll<ICommonContainer<GR, OId<T>, FILT>, GR, OId<T>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<GR>.ensureIndexes() {
        ensureUniqueIndex(IRoleInGroup<T, GOU>::groupOfUserId, IRoleInGroup<T, GOU>::appRoleId)
    }
}
