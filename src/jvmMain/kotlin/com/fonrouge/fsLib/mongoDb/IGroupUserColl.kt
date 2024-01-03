package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupUser
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IGroupUserColl<GU : IGroupUser, FILT : IApiFilter>(klass: KClass<GU>) : Coll<GU, OId<IGroupUser>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<GU>.ensureIndexes() {
        ensureUniqueIndex(IGroupUser::description)
    }
}
