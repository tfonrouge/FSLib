package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import kotlin.reflect.KProperty1

class LookupBuilder<T : BaseModel<*>, U : BaseModel<W>, V : Any, W : Any>(
    private val cTableDb: CTableDb<U, W>,
    private val localField: KProperty1<T, V?>,
    private val foreignField: KProperty1<U, V>,
    val resultProperty: KProperty1<T, U?>,
    private val matchFilters: List<Bson>? = null
) {
    internal fun addToPipeline(pipeline: MutableList<Bson>, modelLookup: ModelLookup<*, *>) {
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

        pip2 += cTableDb.buildLookup(modelLookup.modelLookupList)

        pipeline += lookup(
            from = cTableDb.collection.namespace.collectionName,
            let = listOf(Variable("letVar1", localField)),
            resultProperty = resultProperty,
            *pip2.toTypedArray()
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }
}
