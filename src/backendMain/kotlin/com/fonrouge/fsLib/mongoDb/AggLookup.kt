package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.UnwindOptions
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.lookup
import org.litote.kmongo.unwind
import kotlin.reflect.KProperty1

class AggLookup<S : Any, T : Any>(
    private val from: CoroutineCollection<T>,
    private val localField: KProperty1<S, Any?>,
    private val foreignField: KProperty1<T, *>,
    private val newAs: KProperty1<S, T?>,
) {
    fun addToPipeline(pipeline: MutableList<Bson>) {
        pipeline.add(
            lookup(
                from = from.collection.namespace.collectionName,
                localField = localField.name,
                foreignField = foreignField.name,
                newAs = newAs.name
            )
        )
        pipeline.add(
            unwind(
                fieldName = "\$${newAs.name}",
                unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(true)
            )
        )
    }
}
