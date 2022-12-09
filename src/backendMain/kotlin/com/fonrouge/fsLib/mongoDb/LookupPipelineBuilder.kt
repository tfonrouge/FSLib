package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.mongoDb.CTableDb.Companion.map1
import com.mongodb.client.model.UnwindOptions
import org.bson.conversions.Bson
import org.litote.kmongo.unwind
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@Suppress("unused")
fun <T : BaseModel<*>, U : BaseModel<W>, W : Any> lookupField(
    cTableDb: KClass<out CTableDb<U, W>>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<T, U?>,
    preserveNullAndEmptyArrays: Boolean = true,
): LookupPipelineBuilder<T, U, W> {
    return object : LookupPipelineBuilder<T, U, W>(
        cTableDb = cTableDb,
        localField = localField,
        foreignField = foreignField,
        pipeline = pipeline,
        resultProperty = resultField,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = 1,
        resultUnit = ResultUnit.One
    ) {}
}

@Suppress("unused")
fun <T : BaseModel<*>, U : BaseModel<W>, W : Any> lookupFieldArray(
    cTableDb: KClass<out CTableDb<U, W>>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    pipeline: List<Bson>? = null,
    resultFieldArray: KProperty1<T, Array<U>?>,
    preserveNullAndEmptyArrays: Boolean = true,
    limit: Int? = null,
): LookupPipelineBuilder<T, U, W> {
    return object : LookupPipelineBuilder<T, U, W>(
        cTableDb = cTableDb,
        localField = localField,
        foreignField = foreignField,
        pipeline = pipeline,
        resultProperty = resultFieldArray,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = limit,
        resultUnit = ResultUnit.List
    ) {}
}

abstract class LookupPipelineBuilder<T : BaseModel<*>, U : BaseModel<W>, W : Any>(
    private val cTableDb: KClass<out CTableDb<U, W>>,
    private val localField: KProperty<*>,
    private val foreignField: KProperty<*>,
    private val pipeline: List<Bson>? = null,
    internal val resultProperty: KProperty1<T, *>,
    internal val preserveNullAndEmptyArrays: Boolean = true,
    internal val limit: Int? = null,
    val resultUnit: ResultUnit,
) {
    internal fun pipelineList(modelLookup: ModelLookup<*, *>? = null): List<Bson> {
        val pip2 = mutableListOf<Bson>()
        this.pipeline?.let { bsonList -> pip2 += bsonList }
        map1[cTableDb]?.buildLookupList(*(modelLookup?.modelLookups ?: emptyArray()))?.let { bsonList ->
            pip2 += bsonList
        }
        val pipeline = mutableListOf<Bson>()
        pipeline += lookup(pip2)
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

    fun lookup(pipeline: List<Bson>? = null): Bson {
        val collectionName = map1[cTableDb]?.mongoColl?.namespace?.collectionName
        return lookup5(
            from = collectionName ?: "?",
            localField = localField.name,
            foreignField = foreignField.name,
            pipeline = pipeline ?: this.pipeline,
            newAs = resultProperty.name,
        )
    }

    enum class ResultUnit {
        One,
        List,
    }
}
