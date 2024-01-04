package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IGroupOfUserColl<GOU : IGroupOfUser<T>, T : Any, FILT : IApiFilter>(
    klass: KClass<GOU>
) : Coll<GOU, OId<T>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<GOU>.ensureIndexes() {
        ensureUniqueIndex(IGroupOfUser<T>::description)
    }
}
