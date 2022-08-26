package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.mongoDb.CTableDb.Companion.map1
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class LookupBuilder<T : BaseModel<*>, U : BaseModel<W>, V : Any, W : Any>(
    private val cTableDb: KClass<out CTableDb<U, W>>,
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

        map1[cTableDb]?.buildLookup(*modelLookup.modelLookup)?.let {
            pip2 += it
        }

        pipeline += lookup(
            from = map1[cTableDb]?.mongoColl?.namespace?.collectionName ?: "?",
            let = listOf(Variable("letVar1", localField)),
            resultProperty = resultProperty,
            *pip2.toTypedArray()
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }
}
