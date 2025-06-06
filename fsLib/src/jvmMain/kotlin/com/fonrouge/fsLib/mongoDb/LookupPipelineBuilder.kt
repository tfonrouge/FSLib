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
 * Builds a lookup aggregation pipeline to join documents from a foreign collection.
 *
 * @param coll the foreign collection to join with
 * @param localField the field from the local collection to match with the foreign collection
 * @param foreignField the field from the foreign collection to match with the local collection
 * @param let optional variable definitions for use within the aggregation pipeline
 * @param pipeline optional additional pipeline stages to apply to the foreign collection before joining
 * @param resultField the field in the local collection where the results of the lookup will be stored
 * @param preserveNullAndEmptyArrays indicates whether to include local documents even if no matches are found
 * @param addStages An optional list of additional BSON aggregation stages to apply after the lookup.
 * @return a builder for creating and configuring the lookup aggregation pipeline
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    localField: KProperty<*>,
    foreignField: KProperty<*>,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    preserveNullAndEmptyArrays: Boolean = true,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = Coll.ResultUnit.Single,
    addStages = addStages,
) {}

/**
 * Constructs a `LookupPipelineBuilder` for performing a lookup operation on the specified collection.
 *
 * @param coll The target collection containing the data to perform the lookup against.
 * @param let A list of variables to include in the let statement for the lookup operation.
 * @param pipeline An optional aggregation pipeline to apply during the lookup operation.
 * @param resultField The field in the local collection where the lookup result will be stored.
 * @param preserveNullAndEmptyArrays A boolean indicating whether to include documents in the result
 *                                   when there are no matches in the foreign collection (default is true).
 * @param addStages An optional list of additional BSON aggregation stages to apply after the lookup.
 * @return A `LookupPipelineBuilder` instance to define and process the lookup operation.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    let: List<Variable<out Any>>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, U?>,
    preserveNullAndEmptyArrays: Boolean = true,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = null,
    foreignField = null,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = Coll.ResultUnit.Single,
    addStages = addStages,
) {}

/**
 * Constructs a lookup pipeline to link documents between collections based on specified field references
 * and optional aggregation pipeline stages. Primarily used to build references and retrieve related data
 * during aggregation queries.
 *
 * Commonly used with a group stage in the pipeline for aggregating and retrieving
 * summarized data from document collections.
 *
 * Note: The internal processing pipeline does not utilize any collection-level
 * configuration settings specified in the [coll] parameter beacuse the resulting document is not expected
 * to be the same type of the one defined in the [Coll.commonContainer]
 *
 * @param coll The collection to perform the lookup on. It should be a valid reference to a collection
 * derived from the `ICommonContainer` interface.
 * @param localField The local field in the current collection to match with the field in the foreign collection.
 * Defaults to `null` if not specified.
 * @param foreignField The field in the foreign collection to match with the local field. Defaults to `null`
 * if not specified.
 * @param let A list of variables to be defined in the aggregation pipeline for use in the lookup query. Defaults
 * to `null` if not set.
 * @param pipeline An optional list of additional aggregation pipeline stages to apply in the lookup transformation.
 * Defaults to `null` if not provided.
 * @param resultField The field in the resulting documents where the lookup results will be stored.
 * @param preserveNullAndEmptyArrays Indicates whether to include documents that match the lookup criteria
 * but where no matches are found (resulting in `null` or an empty array). Defaults to `true`.
 * @param addStages An optional list of additional BSON aggregation stages to apply after the lookup.
 * @return A `LookupPipelineBuilder` instance configured to perform the defined lookup with optional field
 * mappings and pipeline procedures.
 */
@Suppress("unused")
fun <T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any> lookupAnyField(
    coll: Coll<out ICommonContainer<U, ID, *>, U, ID, *>,
    localField: KProperty<*>? = null,
    foreignField: KProperty<*>? = null,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, Any?>,
    preserveNullAndEmptyArrays: Boolean = true,
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultField,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = 1,
    resultUnit = Coll.ResultUnit.Single,
    isAnyResult = true,
    addStages = addStages,
) {}

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
 * @param addStages An optional list of additional BSON aggregation stages to apply after the lookup.
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
    addStages: List<Bson>? = null,
): LookupPipelineBuilder<T, U, ID> = object : LookupPipelineBuilder<T, U, ID>(
    coll = coll,
    localField = localField,
    foreignField = foreignField,
    let = let,
    pipeline = pipeline,
    resultProperty = resultFieldArray,
    preserveNullAndEmptyArrays = preserveNullAndEmptyArrays,
    limit = limit,
    resultUnit = Coll.ResultUnit.List,
    addStages = addStages,
) {}

/**
 * Abstract builder class responsible for constructing MongoDB aggregation pipelines
 * for lookups between documents. This enables aggregation stages for joining, filtering,
 * and transforming data based on relationships between collections.
 *
 * @param T The type of the base document in the current collection.
 * @param U The type of the base document in the foreign (lookup) collection.
 * @param ID The type of the unique identifier for documents.
 * @property coll The collection object representing the foreign (lookup) collection and its metadata.
 * @property localField The local field in the current collection used for the lookup.
 * @property foreignField The corresponding field in the foreign collection used for the lookup.
 * @property let List of variables for aggregation stages to be used during the lookup.
 * @property pipeline A list of Bson stages representing the partial pipeline to use in the lookup.
 * @property resultProperty The property in the base document where the result of the lookup will be stored.
 * @property preserveNullAndEmptyArrays Flag indicating whether null or empty array results should be preserved.
 * @property limit An optional limit on the number of results from the lookup.
 * @property resultUnit Specifies whether the lookup result should treat data as a single or multiple entry.
 * @property isAnyResult A flag indicating whether to bypass the standard pipeline processing and return unfiltered results. When true, the [Coll.pipeline] function is not called to construct the pipeline stages.
 * @property addStages An optional list of additional BSON aggregation stages to apply after the lookup.
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
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
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
