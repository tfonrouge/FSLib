package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import kotlin.reflect.KProperty1

class LookupBuilder<T : Any, S>(
    private val collection: CTable<T>,
    private val localField: KProperty1<S, *>,
    private val foreignField: KProperty1<T, *>,
    private val resultProperty: KProperty1<S, *>,
    private val matchFilters: List<Bson>? = null
) {
    fun addToPipeline(pipeline: MutableList<Bson>) {
        val match = mutableListOf(
            expr(
                eq from listOf(
                    foreignField,
                    "$\$letVar1"
                )
            )
        )
        matchFilters?.let { match.addAll(it) }
        pipeline += lookup(
            from = collection.collection.namespace.collectionName,
            let = listOf(Variable("letVar1", localField)),
            resultProperty = resultProperty,
            match(*match.toTypedArray())
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }
}
