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
 * Constructs a `LookupPipelineBuilder` instance for performing a lookup operation between collections.
 *
 * @param coll The collection containing the foreign documents to be joined.
 * @param localField The property in the local collection used to match documents.
 * @param foreignField The property in the foreign collection used to match documents.
 * @param let List of optional variables accessible in the pipeline stages.
 * @param pipeline Custom stages to apply to the foreign documents as part of the lookup.
 * @param resultField The property in the local collection where the joined result will be assigned.
 * @param lookupWrappers Additional wrappers or transformations for the lookup output.
 * @param preserveNullAndEmptyArrays Whether to include documents even if no matches are found in the foreign collection.
 * @param addStages Additional stages to add to the aggregation pipeline.
 * @return A `LookupPipelineBuilder` for chaining or executing the lookup logic.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    preserveNullAndEmptyArrays: Boolean = true,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    lookupWrappers = lookupWrappers,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = Coll.ResultUnit.Single,
    addStages = addStages,
) {}

/**
 * Builds a lookup pipeline for performing aggregation operations.
 *
 * @param coll The collection representing the foreign collection to perform the lookup against.
 * @param let List of variables that can be used in the aggregation pipeline for the lookup.
 * @param pipeline An optional aggregation pipeline to apply to the lookup operation.
 * @param resultField The property in the current collection to store the result of the lookup operation.
 * @param lookupWrappers A list of additional lookup wrapper configurations to apply.
 * @param preserveNullAndEmptyArrays Flag indicating whether to preserve documents that do not have a corresponding match in the lookup.
 * @param addStages An optional list of additional aggregation stages to include after the lookup.
 * @return A LookupPipelineBuilder instance configured with the provided parameters.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    let: List<Variable<out Any>>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    preserveNullAndEmptyArrays: Boolean = true,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = null,
    foreignField = null,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    lookupWrappers = lookupWrappers,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = Coll.ResultUnit.Single,
    addStages = addStages,
) {}

/**
 * Constructs a `LookupPipelineBuilder` to perform a MongoDB aggregation framework lookup operation.
 *
 * @param coll the collection that serves as the target of the lookup operation
 * @param localField the local field in the current collection to use for the lookup, optional
 * @param foreignField the foreign field in the target collection to match with the local field, optional
 * @param let variables that can be referenced in the pipeline stages of the lookup, optional
 * @param pipeline additional aggregation pipeline stages to be applied to the foreign collection in the lookup, optional
 * @param resultField the property to map the resulting lookup results to
 * @param lookupWrappers a list of additional lookup wrappers for customization, defaults to an empty list
 * @param preserveNullAndEmptyArrays whether to include results when no matches are found (true) or exclude them (false), defaults to true
 * @param resultUnit the unit type indicating how the results should be produced (single or multiple), defaults to single
 * @param addStages additional aggregation stages to be appended after the primary lookup operation, optional
 * @return a `LookupPipelineBuilder` configured for the specified lookup operation
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupAnyField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    localField: KProperty<*>? = null,
    foreignField: KProperty<*>? = null,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, Any?>,
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    preserveNullAndEmptyArrays: Boolean = true,
    resultUnit: Coll.ResultUnit = Coll.ResultUnit.Single,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    lookupWrappers = lookupWrappers,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = resultUnit,
    isAnyResult = true,
    addStages = addStages,
) {}

/**
 * Constructs a lookup aggregation pipeline that links documents in the input collection to documents
 * in the foreign collection based on a specified local-foreign field relationship. The results of the lookup
 * operation will be mapped into an array field in the resulting documents.
 *
 * @param coll The foreign collection to perform the lookup against.
 * @param localField The field in the input collection used for matching documents in the foreign collection.
 * @param foreignField The field in the foreign collection to match against the local field.
 * @param let Optional list of variables to define in the aggregation pipeline context of the foreign collection.
 * @param pipeline Optional aggregation pipeline stages to apply to the foreign collection before linking.
 * @param resultFieldArray The property in the resulting document where the array of matching documents should be stored.
 * @param lookupWrappers Optional list of additional lookup configurations for nested or extended lookups.
 * @param preserveNullAndEmptyArrays If true, documents without matches will include an empty array; otherwise, such documents are omitted.
 * @param limit Optional limit on the number of matching documents to include in the result array.
 * @param addStages Optional list of additional aggregation stages to be appended to the pipeline after the lookup stage.
 * @return An instance of [LookupPipelineBuilder] configured for the specified lookup requirements.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupFieldArray(
    coll: Coll<out ICommonContainer<U, ID, *>, out U, ID, *>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultFieldArray: KProperty1<in T, Collection<U>?>,
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    preserveNullAndEmptyArrays: Boolean = true,
    limit: Int? = null,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultFieldArray,
    lookupWrappers = lookupWrappers,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = limit,
    resultUnit = Coll.ResultUnit.List,
    addStages = addStages,
) {}

/**
 * A builder for constructing MongoDB aggregation pipelines incorporating lookups and transformations.
 *
 * This class is designed to create dynamic aggregation pipelines based on predefined configurations,
 * relationships between fields in collections, and result handling parameters. It allows for the
 * specification of lookup fields, conditional pipeline stages, and other MongoDB aggregation behaviors.
 *
 * @param T The type of the main document involved in the aggregation pipeline. Inherits from BaseDoc.
 * @param U The type of the foreign document referenced in the lookup. Inherits from BaseDoc.
 * @param ID The type of identifier used by the documents in the collections.
 * @param coll The collection interface for accessing the foreign collection involved in the lookup.
 * @param localField The local field in the main collection that establishes the relationship with the foreign collection.
 *                   Can be null if using alternative lookup mechanisms.
 * @param foreignField The foreign field in the referenced collection that establishes the relationship with the main document.
 *                     Can be null if using alternative lookup mechanisms.
 * @param let Variables to pass to the lookup pipeline as part of a `$lookup` stage.
 * @param pipeline The aggregation stages to integrate with the lookup pipeline.
 * @param resultProperty The property in the main document where lookup results are stored.
 * @param lookupWrappers A list of nested lookup wrappers to create complex join logic.
 * @param preserveNullAndEmptyArrays A flag indicating whether to preserve null or empty arrays in the resulting joined data.
 * @param limit An optional limit to apply to the aggregation results.
 * @param resultUnit Specifies the unit of result extraction (e.g., single or multiple).
 * @param isAnyResult A flag to indicate whether the lookup should consider any result as valid.
 * @param addStages Additional BSON stages to append to the final aggregation pipeline.
 */
abstract class LookupPipelineBuilder<T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any>(
    private val coll: Coll<out ICommonContainer<U, ID, *>, out U, ID, *>,
    private val localField: KProperty<*>?,
    private val foreignField: KProperty<*>?,
    private val let: List<Variable<out Any>>? = null,
    private val pipeline: List<Bson>?,
    val resultProperty: KProperty1<in T, *>,
    val lookupWrappers: List<LookupWrapper<*, *>>,
    internal val preserveNullAndEmptyArrays: Boolean,
    internal val limit: Int?,
    val resultUnit: Coll.ResultUnit,
    private val isAnyResult: Boolean = false,
    val addStages: List<Bson>? = null,
) {
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
        lookupWrappers: List<LookupWrapper<*, *>> = this.lookupWrappers,
    ): List<Bson> {
        val pip2 = mutableListOf<Bson>()
        this.pipeline?.let { bsonList -> pip2 += bsonList }
        if (!isAnyResult) {
            coll.pipeline(
                lookupWrappers = lookupWrappers,
                resultUnit = resultUnit,
            ).let { it ->
                pip2 += it
                limit?.let { pip2 += limit(it) }
            }
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
        return pipeline + addStages.orEmpty()
    }
}
