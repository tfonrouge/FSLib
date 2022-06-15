package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.limit
import org.litote.kmongo.skip
import kotlin.reflect.KProperty1

class ModelLookup<T : BaseModel<*>, U : BaseModel<*>>(
    val resultProperty: KProperty1<T, U?>,
    val modelLookupList: List<ModelLookup<U, *>>? = null
)

class FirstStage(
    val pipeline: MutableList<Bson>,
    val count: Long,
    val last_page: Int,
    val last_row: Int
)

abstract class CTableDb<T : BaseModel<*>>(
    val collection: CoroutineCollection<T>,
    private val lookupBuilderList: List<LookupBuilder<T, *, *>>? = null,
) {

    @Suppress("unused")
    suspend inline fun <reified U : T> aggregateToRemoteData(
        firstStage: FirstStage,
        modelLookupList: List<ModelLookup<*, *>>? = null
    ): RemoteData<U> {
        firstStage.pipeline.addAll(buildLookup(modelLookupList))
        val list = collection.aggregate<U>(firstStage.pipeline).toList()
        return RemoteData(data = list, last_page = firstStage.last_page, last_row = firstStage.last_row)
    }

    fun buildLookup(modelLookupList: List<ModelLookup<*, *>>? = null): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookupBuilderList?.forEach { lookupBuilder ->
            modelLookupList?.firstOrNull { lookupBuilder.resultProperty == it.resultProperty }
                ?.let { modelLookup: ModelLookup<*, *> ->
                    lookupBuilder.addToPipeline(pipeline, modelLookup)
                }
        }
        return pipeline
    }

    @Suppress("unused")
    suspend fun buildFirstStage(
        match: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
        state: String? = null
    ): FirstStage {
        val bsonList = mutableListOf<Bson>()
        match?.let {
            bsonList.add(it)
        }
        var filterValue: Document? = null
        if (filter != null && filter.isNotEmpty()) {
            filterValue = Document()
            filter.forEach { remoteFilter ->
                val value = when (remoteFilter.type) {
                    "like" -> Document("\$regex", remoteFilter.value).append("\$options", "i")
                    else -> remoteFilter.value
                }
                filterValue.append(remoteFilter.field, value)
            }
        }
        val count: Long = filterValue?.let { collection.countDocuments(it) } ?: collection.countDocuments()
        if (page == null) {
            return FirstStage(
                pipeline = bsonList,
                count = count,
                last_page = -1,
                last_row = -1
            )
        } else {
            val nSize = size ?: 10
            val nSkip = nSize * (page - 1)
            filterValue?.let {
                bsonList.add(Document("\$match", it))
            }
            bsonList.add(
                skip(nSkip)
            )
            bsonList.add(
                limit(nSize)
            )
            return FirstStage(
                pipeline = bsonList,
                count = count,
                last_page = (count / nSize + 1).toInt(),
                last_row = 5
            )
        }
    }
}
