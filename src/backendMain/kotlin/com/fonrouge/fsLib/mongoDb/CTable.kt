package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineAggregatePublisher
import org.litote.kmongo.coroutine.CoroutineCollection

abstract class CTable<T : Any>(
    val collection: CoroutineCollection<T>,
    val lookupBuilderList: List<LookupBuilder<*, T>>? = null
) {

    @Suppress("unused")
    inline fun <reified U : T> aggregate(pipeline: MutableList<Bson> = mutableListOf()): CoroutineAggregatePublisher<U> {
        lookupBuilderList?.forEach { lookupBuilder ->
            lookupBuilder.addToPipeline(pipeline)
        }
        return collection.aggregate(pipeline)
    }
}
