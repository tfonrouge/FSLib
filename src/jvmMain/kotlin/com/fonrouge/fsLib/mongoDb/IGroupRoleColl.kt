package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupRole
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IGroupRoleColl<GR : IGroupRole, FILT : IApiFilter>(klass: KClass<GR>) : Coll<GR, OId<IGroupRole>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<GR>.ensureIndexes() {
        ensureUniqueIndex(IGroupRole::groupUserId, IGroupRole::appRoleId)
    }
}
