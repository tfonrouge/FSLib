package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IGroupRole
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection

@Suppress("unused")
abstract class IGroupRoleColl<GR : IGroupRole<T, GOU>, T : Any, GOU : IGroupOfUser<*>, FILT : IApiFilter>(
    commonContainer: ICommonContainer<GR, OId<T>, FILT>
) : Coll<ICommonContainer<GR, OId<T>, FILT>, GR, OId<T>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<GR>.ensureIndexes() {
        ensureUniqueIndex(IGroupRole<T, GOU>::groupOfUserId, IGroupRole<T, GOU>::appRoleId)
    }
}
