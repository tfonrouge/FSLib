package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IGroupOfUser
import com.fonrouge.fsLib.serializers.OId
import org.litote.kmongo.coroutine.CoroutineCollection

@Suppress("unused")
abstract class IGroupOfUserColl<GOU : IGroupOfUser<T>, T : Any, FILT : IApiFilter>(
    commonContainer: ICommonContainer<GOU, OId<T>, FILT>
) : Coll<GOU, OId<T>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<GOU>.ensureIndexes() {
        ensureUniqueIndex(IGroupOfUser<T>::description)
    }
}
