package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.base.BaseDoc
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.limit
import org.litote.kmongo.unwind
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Constructs a lookup pipeline between two collections based on the specified fields.
 *
 * @param coll The collection to perform the lookup on.
 * @param localField The local field used for the join condition.
 * @param foreignField The foreign field used for the join condition.
 * @param let Additional variables to use in the operation, or null.
 * @param pipeline Optional aggregation pipeline to apply to the foreign collection before the join.
 * @param resultField The property in the result document where the joined documents will be stored.
 * @param limit The maximum number of matching documents to include for each input document, defaults to 1.
 * @param preserveNullAndEmptyArrays Whether to include documents in the output array that do not have matching documents,
 *     defaults to true.
 * @return A configured `LookupPipelineBuilder` to execute the lookup operation.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    limit: Int? = 1,
    preserveNullAndEmptyArrays: Boolean = true,
): LookupPipelineBuilder<T, U, ID> {
    return object : LookupPipelineBuilder<T, U, ID>(
        coll = coll,
        localField = localField,
        foreignField = foreignField,
        let = let,
        pipeline = pipeline,
        resultProperty = resultField,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = limit,
        resultUnit = Coll.ResultUnit.Single
    ) {}
}

/**
 * Builds a pipeline for a lookup operation by defining the necessary conditions
 * and fields to retrieve specific results from the given collection.
 *
 * @param T The type of the local document involved in the lookup operation.
 * @param U The type of the foreign document to be looked up.
 * @param ID The type of the unique identifier used in both local and foreign documents.
 * @param coll The collection providing the documents to be looked up.
 * @param let A list of variables used for the lookup operation.
 * @param pipeline An optional aggregation pipeline to apply on the foreign collection during the lookup.
 * @param resultField The target property in the local document where the lookup results will be mapped.
 * @param limit The maximum number of documents to retrieve from the foreign collection for each local document, defaults to 1.
 * @param preserveNullAndEmptyArrays Defines whether to preserve "null" or empty arrays in the lookup result, defaults to true.
 * @return A configured instance of LookupPipelineBuilder to execute the lookup operation.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    let: List<Variable<out Any>>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    limit: Int? = 1,
    preserveNullAndEmptyArrays: Boolean = true,
): LookupPipelineBuilder<T, U, ID> {
    return object : LookupPipelineBuilder<T, U, ID>(
        coll = coll,
        localField = null,
        foreignField = null,
        let = let,
        pipeline = pipeline,
        resultProperty = resultField,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = limit,
        resultUnit = Coll.ResultUnit.Single
    ) {}
}

/**
 * Constructs and returns a `LookupPipelineBuilder` with the specified parameters to facilitate MongoDB lookup aggregation.
 *
 * @param coll The collection on which the lookup operation is to be performed.
 * @param localField The field in the input documents to match against the `foreignField`.
 * @param foreignField The field in the documents in the `from` collection.
 * @param let Optional variables that can be accessed within the aggregation pipeline.
 * @param pipeline Optional aggregation pipeline to apply to the documents in the `from` collection.
 * @param resultFieldArray The field in the input documents where the results of the lookup will be stored.
 * @param preserveNullAndEmptyArrays If true, the join will include documents with null and empty arrays.
 * @param limit Optional limit on the number of results to return.
 * @return A `LookupPipelineBuilder` configured with the provided parameters.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupFieldArray(
    coll: Coll<out ICommonContainer<U, ID, *>, out U, ID, *>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultFieldArray: KProperty1<in T, Collection<U>?>,
    preserveNullAndEmptyArrays: Boolean = true,
    limit: Int? = null,
): LookupPipelineBuilder<T, U, ID> {
    return object : LookupPipelineBuilder<T, U, ID>(
        coll = coll,
        localField = localField,
        foreignField = foreignField,
        let = let,
        pipeline = pipeline,
        resultProperty = resultFieldArray,
        preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
        limit = limit,
        resultUnit = Coll.ResultUnit.List
    ) {}
}

/**
 * Abstract class that builds a MongoDB lookup pipeline for aggregating documents.
 *
 * @param coll The collection containing common container documents.
 * @param localField The field from the local collection to match values against.
 * @param foreignField The field from the foreign collection to match values against.
 * @param let Optional list of variables to use in the $lookup stage.
 * @param pipeline Optional list of BSON operations for the pipeline stage.
 * @param resultProperty The property of the result to project.
 * @param preserveNullAndEmptyArrays Flag to control whether to preserve null and empty array values.
 * @param limit Optional limit to the number of documents in the aggregation stage.
 * @param resultUnit Specifies the result unit type for the lookup aggregation.
 */
abstract class LookupPipelineBuilder<T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any>(
    private val coll: Coll<out ICommonContainer<U, ID, *>, out U, ID, *>,
    private val localField: KProperty<*>?,
    private val foreignField: KProperty<*>?,
    private val let: List<Variable<out Any>>? = null,
    private val pipeline: List<Bson>?,
    val resultProperty: KProperty1<in T, *>,
    internal val preserveNullAndEmptyArrays: Boolean,
    internal val limit: Int?,
    val resultUnit: Coll.ResultUnit,
) {
    /**
     * Builds a list of BSON pipeline stages using the provided list of lookup wrappers.
     * The method creates a MongoDB aggregation pipeline by processing the nested `LookupWrapper` instances.
     *
     * @param lookupWrappers A list of `LookupWrapper` instances that define the lookup and pipeline details
     *                       for generating the aggregation stages.
     * @return A list of `Bson` objects representing the aggregation pipeline stages.
     */
//    @Suppress("unused")
//    fun <U : BaseDoc<*>, V : BaseDoc<*>> toPipeline(
//        lookupWrappers: List<LookupWrapper<V, U>>
//    ): List<Bson> = toPipeline(LookupWrapper<U, V>(lookupWrappers))

    /**
     * Constructs a MongoDB aggregation pipeline based on the provided lookup wrapper and class fields.
     *
     * The method combines predefined pipelines, lookup configurations, and conditional stages into a single
     * list of BSON objects representing the aggregation pipeline. It ensures that the pipeline is dynamically
     * adapted based on the input and context-specific parameters.
     *
     * @param lookup A lookup wrapper instance containing nested lookup definitions and pipeline details.
     *               Defaults to null if no additional lookups are required.
     * @return A list of BSON objects representing the final aggregation pipeline stages.
     */
    fun toPipeline(
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    ): List<Bson> {
        val pip2 = mutableListOf<Bson>()
        this.pipeline?.let { bsonList -> pip2 += bsonList }
        coll.pipeline(
            lookupWrappers = lookupWrappers,
            resultUnit = resultUnit,
        ).let { it ->
            pip2 += it
            limit?.let { pip2 += limit(it) }
        }
        val pipeline = mutableListOf<Bson>()
        if (localField != null && foreignField != null) {
            if (pip2.isEmpty()) {
                pipeline += lookup(
                    from = coll.mongoColl.namespace.collectionName,
                    localField = localField,
                    foreignField = foreignField,
                    resultField = resultProperty,
                )
            } else {
                pipeline += lookup5(
                    from = coll.mongoColl.namespace.collectionName,
                    localField = localField,
                    foreignField = foreignField,
                    let = let,
                    resultField = resultProperty,
                    pipeline = pip2
                )
            }
        } else {
            pipeline += org.litote.kmongo.lookup(
                from = coll.mongoColl.namespace.collectionName,
                let = let,
                resultProperty = resultProperty,
                pipeline = pip2.toTypedArray()
            )
        }
        if (resultUnit == Coll.ResultUnit.Single) {
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
}
