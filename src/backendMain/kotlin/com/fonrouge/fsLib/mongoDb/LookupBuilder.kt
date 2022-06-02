package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import kotlin.reflect.KProperty1

class LookupBuilder<T : Any, S>(
    private val cTableDb: CTableDb<T>,
    private val localField: KProperty1<S, *>,
    private val foreignField: KProperty1<T, *>,
    val resultProperty: KProperty1<S, *>,
    private val matchFilters: List<Bson>? = null
) {
    internal fun addToPipeline(pipeline: MutableList<Bson>, includeList: List<Lookup<out Any?, out Any?>>?) {
        val match = mutableListOf(
            expr(
                eq from listOf(
                    foreignField,
                    "$\$letVar1"
                )
            )
        )

        matchFilters?.let { match.addAll(it) }

        val pip2 = mutableListOf(
            match(*match.toTypedArray())
        )

        pip2 += cTableDb.buildLookup(includeList)

        pipeline += lookup(
            from = cTableDb.collection.namespace.collectionName,
            let = listOf(Variable("letVar1", localField)),
            resultProperty = resultProperty,
            *pip2.toTypedArray()
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }
}
