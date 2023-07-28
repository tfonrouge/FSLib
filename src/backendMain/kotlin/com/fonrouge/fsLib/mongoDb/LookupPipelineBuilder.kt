package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.mongoDb.Coll.Companion.collMap
import com.mongodb.client.model.UnwindOptions
import org.bson.conversions.Bson
import org.litote.kmongo.limit
import org.litote.kmongo.unwind
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    collKClass: KClass<out Coll<U, ID, *>>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    preserveNullAndEmptyArrays: Boolean = true,
): LookupPipelineBuilder<T, U, ID> {
    return object : LookupPipelineBuilder<T, U, ID>(
        collKClass = collKClass,
        localField = localField,
        foreignField = foreignField,
        pipeline = pipeline,
        resultProperty = resultField,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = 1,
        resultUnit = ResultUnit.One /* TODO: Make this impose a {$limit: 1} on returned data */
    ) {}
}

@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupFieldArray(
    collKClass: KClass<out Coll<U, ID, *>>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    pipeline: List<Bson>? = null,
    resultFieldArray: KProperty1<in T, Array<U>?>,
    preserveNullAndEmptyArrays: Boolean = true,
    limit: Int? = null,
): LookupPipelineBuilder<T, U, ID> {
    return object : LookupPipelineBuilder<T, U, ID>(
        collKClass = collKClass,
        localField = localField,
        foreignField = foreignField,
        pipeline = pipeline,
        resultProperty = resultFieldArray,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = limit,
        resultUnit = ResultUnit.List
    ) {}
}

abstract class LookupPipelineBuilder<T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any>(
    private val collKClass: KClass<out Coll<U, ID, *>>,
    private val localField: KProperty<*>,
    private val foreignField: KProperty<*>,
    private val pipeline: List<Bson>? = null,
    internal val resultProperty: KProperty1<in T, *>,
    internal val preserveNullAndEmptyArrays: Boolean = true,
    internal val limit: Int? = null,
    val resultUnit: ResultUnit,
) {
    internal fun pipelineList(lookup: LookupWrapper<*, *>? = null): List<Bson> {
        val pip2 = mutableListOf<Bson>()
        this.pipeline?.let { bsonList -> pip2 += bsonList }
        collMap[collKClass]?.let { coll ->
            coll.finalPipeline(
                pipeline = mutableListOf(),
                lookups = lookup?.lookupWrappers,
                apiFilter = null
            ).let {
                pip2 += it
                if (resultUnit == ResultUnit.One) pip2 += limit(1)
            }
        }
        val pipeline = mutableListOf<Bson>()
        if (pip2.isEmpty()) {
            pipeline += lookup(
                from = collMap[collKClass]?.mongoColl?.namespace?.collectionName ?: throw Exception(),
                localField = localField,
                foreignField = foreignField,
                resultField = resultProperty,
            )
        } else {
            pipeline += lookup(
                from = collMap[collKClass]?.mongoColl?.namespace?.collectionName ?: throw Exception(),
                localField = localField,
                foreignField = foreignField,
                resultField = resultProperty,
                pipeline = pip2
            )
        }
        if (resultUnit == ResultUnit.One) {
            resultProperty.let {
                pipeline += resultProperty.unwind(
                    UnwindOptions().preserveNullAndEmptyArrays(
                        preserveNullAndEmptyArrays
                    )
                )
            }
        }
        return pipeline
    }

    enum class ResultUnit {
        One,
        List,
    }
}
