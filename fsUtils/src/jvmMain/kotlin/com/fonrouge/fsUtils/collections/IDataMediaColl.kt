package com.fonrouge.fsUtils.collections

import com.fonrouge.fullStack.mongoDb.AssignTo
import com.fonrouge.fullStack.mongoDb.IChangeLogColl
import com.fonrouge.fullStack.mongoDb.LookupPipelineBuilder
import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.model.IUser
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.types.StringId
import com.fonrouge.fullStack.common.ICommonChangeLog
import com.fonrouge.fsUtils.common.ICommonDataMedia
import com.fonrouge.fsUtils.model.DataMediaFilter
import com.fonrouge.fullStack.model.IChangeLog
import com.fonrouge.fsUtils.model.IDataMedia
import io.ktor.server.application.*
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import java.io.File
import kotlin.reflect.KProperty1

@Suppress("unused")
abstract class IDataMediaColl<ChgLogColl : IChangeLogColl<CmnChgLog, ChgLog, U, UID>, CmnChgLog : ICommonChangeLog<ChgLog, U, UID>, ChgLog : IChangeLog<U, UID>, CC : ICommonDataMedia<DM, U, UID>, DM : IDataMedia<U, UID>, U : IUser<UID>, UID : Any>(
    commonContainer: CC,
    changeLogColl: () -> ChgLogColl,
) : IWorkLogBaseColl<ChgLogColl, CmnChgLog, ChgLog, U, UID, CC, DM, StringId<IDataMedia<U, UID>>, DataMediaFilter>(
    commonContainer = commonContainer,
    changeLogColl = changeLogColl,
) {
    override fun fixedLookupList(apiFilter: DataMediaFilter): List<KProperty1<in IDataMedia<U, UID>, *>>? = listOf(
        IDataMedia<*, *>::user,
    )

    override suspend fun CoroutineCollection<DM>.indexes() {
        ensureIndex(
            IDataMedia<*, *>::classifierDoc,
            IDataMedia<*, *>::serializedIdDoc,
            IDataMedia<*, *>::order,
            IDataMedia<*, *>::fechaCreacion
        )
    }

    abstract val lookupUser: () -> LookupPipelineBuilder<DM, U, UID>

    override val lookupFun: (DataMediaFilter) -> List<LookupPipelineBuilder<DM, *, *>> = {
        listOf(
            lookupUser()
        )
    }

    override fun matchStage(call: ApplicationCall?, apiFilter: DataMediaFilter, resultUnit: ResultUnit): Bson? {
        val matchDoc = mutableListOf<Bson>()
        apiFilter.classifierDoc?.let { matchDoc.add(IDataMedia<*, *>::classifierDoc eq it) }
        apiFilter.serializedIdDoc?.let { matchDoc.add(IDataMedia<*, *>::serializedIdDoc eq it) }
        return and(matchDoc)
    }

    override suspend fun onAfterDeleteAction(
        apiItem: ApiItem.Action.Delete<DM, StringId<IDataMedia<U, UID>>, DataMediaFilter>,
        result: Boolean,
    ) {
        File(apiItem.item.filePath).delete()
        File(apiItem.item.thumbnailPath).delete()
    }

    override fun sortStage(call: ApplicationCall?, apiFilter: DataMediaFilter): Bson? {
        return ascending(IDataMedia<*, *>::order, IDataMedia<*, *>::fechaCreacion)
    }

    suspend fun updateOrder(
        call: ApplicationCall,
        id: StringId<IDataMedia<U, UID>>,
        order: String?,
    ): SimpleState = updateFieldsById(
        call = call,
        id = id,
        AssignTo(IDataMedia<U, UID>::order, order)
    ).asSimpleState
}
