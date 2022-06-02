package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineAggregatePublisher
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.json
import kotlin.reflect.KProperty1

class ModelLookup<T : BaseModel<*>, U : BaseModel<*>>(
    val resultProperty: KProperty1<T, U?>,
    val modelLookupList: List<ModelLookup<U, *>>? = null
)

abstract class CTableDb<T : BaseModel<*>>(
    val collection: CoroutineCollection<T>,
    private val lookupBuilderList: List<LookupBuilder<T, *, *>>? = null,
) {

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
    inline fun <reified U : T> aggregateWithLookup(
        modelLookupList: List<ModelLookup<*, *>>? = null,
        pipeline: MutableList<Bson> = mutableListOf()
    ): CoroutineAggregatePublisher<U> {
        pipeline.addAll(buildLookup(modelLookupList))
        println("PIPELINE:\n${pipeline.json}")
        return collection.aggregate(pipeline)
    }
}
