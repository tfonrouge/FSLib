package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IGroupOfUserColl<GU : IGroupOfUser, FILT : IApiFilter>(klass: KClass<GU>) :
    Coll<GU, OId<IGroupOfUser>, FILT>(
        klass = klass
    ) {
    override suspend fun CoroutineCollection<GU>.ensureIndexes() {
        ensureUniqueIndex(IGroupOfUser::description)
    }
}
