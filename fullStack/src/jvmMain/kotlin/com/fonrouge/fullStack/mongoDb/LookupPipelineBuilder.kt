package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
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
 * Builds a lookup pipeline for querying related fields between collections and returns a `LookupPipelineBuilder` instance.
 *
 * @param coll The collection containing the data to be queried.
 * @param localField The field in the local collection to match when performing the lookup (optional).
 * @param foreignField The field in the foreign collection to match when performing the lookup (optional).
 * @param let A list of variables that can be referenced in the pipeline stages (optional).
 * @param pipeline Additional MongoDB aggregation pipeline stages (optional).
 * @param resultField The target property in the local collection to store the lookup result.
 * @param lookupWrappers A list of `LookupWrapper` objects to manage nested lookups (optional, defaults to an empty list).
 * @param preserveNullAndEmptyArrays Whether to include documents with no matching foreign documents (defaults to true).
 * @param resultUnit Specifies whether the lookup result is a single document or an array of documents.
 * @param unwind Whether to automatically unwind the results of the lookup (optional).
 * @param addStages Additional stages to be appended to the pipeline (optional).
 * @return An instance of `LookupPipelineBuilder` representing the constructed lookup query.
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
    unwind: Boolean? = null,
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
    unwind = unwind,
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
 * A builder class for constructing MongoDB aggregation pipelines involving lookup stages.
 *
 * This class provides functionality to dynamically create pipelines using various parameters like
 * local and foreign fields, additional stages, nested lookups, and other configurations. It is
 * intended to facilitate the generation of robust aggregation pipelines for collections with
 * structured relationships.
 *
 * @param T The type of the primary document associated with this pipeline builder. It must extend the `BaseDoc` interface.
 * @param U The type of the foreign document being referenced in the lookup. It must extend the `BaseDoc` interface.
 * @param ID The type of the identifier used for the documents.
 * @param coll The target collection associated with the foreign documents.
 * @param localField The field in the local collection used for matching documents.
 * @param foreignField The field in the foreign collection being matched against.
 * @param let Optional variables to define within the aggregation pipeline.
 * @param pipeline An optional list of BSON objects representing additional predefined stages.
 * @param resultProperty The property in the local document to store the lookup results.
 * @param lookupWrappers A list of nested `LookupWrapper` instances used for additional lookup stages.
 * @param preserveNullAndEmptyArrays A flag indicating whether to preserve null or empty arrays in the result.
 * @param limit An optional limit to apply to the number of foreign results.
 * @param resultUnit The result type, determining whether a single document or multiple documents are returned.
 * @param unwind An optional flag to determine whether to apply an unwind stage to the lookup results.
 * @param isAnyResult A flag indicating if any results are expected from the lookup stage.
 * @param addStages A list of additional BSON stages to be added to the final pipeline.
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
    val unwind: Boolean? = null,
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
        if ((unwind == null && resultUnit == Coll.ResultUnit.Single) || unwind == true) {
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
