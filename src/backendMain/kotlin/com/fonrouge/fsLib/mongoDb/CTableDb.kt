package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineAggregatePublisher
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.json
import kotlin.reflect.KProperty1

class Lookup<T, S>(
    val resultProperty: KProperty1<T, S?>,
    val lookupList: List<Lookup<S, *>>? = null
)

abstract class CTableDb<T : Any>(
    val collection: CoroutineCollection<T>,
    private val lookupBuilderList: List<LookupBuilder<*, T>>? = null
) {

    fun buildLookup(includeList: List<Lookup<*, *>>? = null): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookupBuilderList?.forEach { lookupBuilder ->
            includeList?.firstOrNull { lookupBuilder.resultProperty == it.resultProperty }?.let {
                lookupBuilder.addToPipeline(pipeline, it.lookupList)
            }
        }
        return pipeline
    }

    @Suppress("unused")
    inline fun <reified U : T> aggregateWithLookup(
        lookupList: List<Lookup<*, *>>? = null,
        pipeline: MutableList<Bson> = mutableListOf()
    ): CoroutineAggregatePublisher<U> {
        pipeline.addAll(buildLookup(lookupList))
        println("PIPELINE:\n${pipeline.json}")
        return collection.aggregate(pipeline)
    }
}
