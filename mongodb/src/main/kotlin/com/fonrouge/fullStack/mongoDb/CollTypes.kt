package com.fonrouge.fullStack.mongoDb

import kotlinx.serialization.Serializable
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.json

/**
 * Specifies the counting strategy used in aggregation pipelines.
 */
@Serializable
enum class CountType {
    PreLookup,
    PostLookup,
    Estimated,
    Unknown
}

/**
 * Specifies whether the pipeline is for a single document or a list.
 */
enum class ResultUnit {
    Single,
    List,
}

/**
 * Holds page count information for paginated aggregation results.
 *
 * @property match Optional BSON match filter for pre-lookup counting.
 * @property pipeline Optional aggregation pipeline for post-lookup counting.
 * @property lastPage The calculated last page number.
 * @property lastRow The total row count.
 * @property countType The counting strategy used.
 */
data class PageCountInfo(
    val match: Bson? = null,
    val pipeline: List<Bson>? = null,
    var lastPage: Int? = null,
    var lastRow: Int? = null,
    val countType: CountType,
) {
    /**
     * Performs the count operation against the collection and calculates pagination info.
     *
     * @param coll The collection to count documents in.
     * @param pageSize The number of items per page.
     */
    suspend fun count(coll: Coll<*, *, *, *, *>, pageSize: Int) {
        val count = when (countType) {
            CountType.PreLookup ->
                coll.mongoColl.coroutine.countDocuments(
                    match?.let {
                        if (Document.parse(match.json).isNotEmpty())
                            it
                        else
                            EMPTY_BSON
                    } ?: EMPTY_BSON
                )

            CountType.PostLookup -> pipeline?.let {
                coll.mongoColl.coroutine.aggregate<Document>(it).first()?.getInteger("count")
                    ?.toLong()
            }

            CountType.Estimated -> coll.mongoColl.coroutine.estimatedDocumentCount()
            CountType.Unknown -> null
        }
        count?.let {
            if (pageSize > 0) {
                lastPage = (count / pageSize + if (count.toInt() % pageSize > 0) 1 else 0).toInt()
            }
            lastRow = it.toInt()
        }
    }
}
