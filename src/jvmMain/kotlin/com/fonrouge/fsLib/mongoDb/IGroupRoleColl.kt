package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.model.base.IGroupRole
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IGroupRoleColl<GR : IGroupRole<T, GOU>, T : Any, GOU : IGroupOfUser<*>, FILT : IApiFilter>(
    klass: KClass<GR>
) : Coll<GR, OId<T>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<GR>.ensureIndexes() {
        ensureUniqueIndex(IGroupRole<T, GOU>::groupOfUserId, IGroupRole<T, GOU>::appRoleId)
    }
}
