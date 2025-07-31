package com.fonrouge.fsUtils.collections

import com.fonrouge.backendLib.mongoDb.Coll
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.types.OId
import com.fonrouge.modelUtils.common.ICommonChangeLog
import com.fonrouge.modelUtils.model.ChangeLogFilter
import com.fonrouge.modelUtils.model.IChangeLog
import io.ktor.server.application.*
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.descending
import org.litote.kmongo.eq

abstract class IChangeLogColl<CC : ICommonChangeLog<CHL, U, UID>, CHL : IChangeLog<U, UID>, U : IUser<UID>, UID : Any>(
    commonContainer: CC,
) : Coll<CC, CHL, OId<IChangeLog<U, UID>>, ChangeLogFilter>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<CHL>.indexes() {
        ensureIndex(IChangeLog<*, *>::className, IChangeLog<*, *>::serializedId, IChangeLog<*, *>::dateTime)
    }

    override fun matchStage(call: ApplicationCall?, apiFilter: ChangeLogFilter, resultUnit: ResultUnit): Bson? {
        val matchDoc = mutableListOf<Bson>()
        apiFilter.action?.let { matchDoc += IChangeLog<*, *>::action eq it }
        apiFilter.className?.let { matchDoc += IChangeLog<*, *>::className eq it }
        apiFilter.serializedId?.let { matchDoc += IChangeLog<*, *>::serializedId eq it }
        return and(matchDoc)
    }

    override fun sortStage(call: ApplicationCall?, apiFilter: ChangeLogFilter): Bson? {
        return descending(IChangeLog<*, *>::dateTime)
    }
}
