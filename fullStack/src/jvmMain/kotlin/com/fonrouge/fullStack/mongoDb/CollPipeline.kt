package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.bson.json2
import com.fonrouge.base.model.BaseDoc
import com.mongodb.client.model.Aggregates
import com.mongodb.reactivestreams.client.AggregatePublisher
import io.ktor.server.application.*
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf

/**
 * Tracks recursive lookup depth per result field to prevent infinite recursion.
 */
internal data class ResultField(
    val threadId: Long = Thread.currentThread().threadId(),
    val kResultField: KProperty1<*, *>,
)

/**
 * Builds the lookup pipeline stages from lookup functions and lookup wrappers.
 *
 * @param lookupWrappers Lookup wrappers defining nested lookups.
 * @param apiFilter The API filter for lookup function resolution.
 * @return A mutable list of BSON stages for the lookup pipeline.
 */
internal fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> Coll<*, T, ID, FILT, *>.buildLookupList(
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    apiFilter: FILT = commonContainer.apiFilterInstance(),
): MutableList<Bson> {
    fun outErr(resultField: ResultField, times: Int) {
        System.err.println("MAX_RECURSIVE_RESULT_FIELD limit exceeded, ${objName}: $resultField -> $times")
    }

    val pipeline: MutableList<Bson> = mutableListOf()
    val lookupPipelineBuilders = lookupFun(apiFilter)
        .associateBy { it.resultProperty.name }
        .plus(
            lookupWrappers.mapNotNull { if (it is LookupByPipeline<*, *, *>) it.pipeline else null }
                .associateBy { it.resultProperty.name }
        )
    lookupPipelineBuilders.forEach { (_, lookupPipelineBuilder) ->
        val lookupWrapper: LookupWrapper<*, *>? = lookupWrappers.find { lookupWrapper: LookupWrapper<*, *> ->
            val kProperty1 = lookupPipelineBuilder.resultProperty
            val owner1 = kProperty1.instanceParameter?.type?.classifier as? KClass<*> ?: return@find false
            when (lookupWrapper) {
                is LookupByProperty<*, *> -> lookupWrapper.resultProperty
                is LookupByPropertyList<*, *> -> lookupWrapper.resultProperty
                is LookupByPipeline<*, *, *> -> lookupWrapper.pipeline.resultProperty as kotlin.jvm.internal.PropertyReference1Impl
                else -> null
            }?.let { kProperty2 ->
                val owner2 = kProperty2.instanceParameter?.type?.classifier as? KClass<*> ?: return@find false
                owner2.isSubclassOf(owner1) && kProperty2.name == kProperty1.name
            } ?: false
        }
        if (lookupWrapper != null) {
            val resultField = ResultField(kResultField = lookupPipelineBuilder.resultProperty)
            val times = resultFieldStack[resultField]?.inc() ?: 1
            resultFieldStack[resultField] = times
            if (times > Coll.MAX_RECURSIVE_RESULT_FIELD) {
                outErr(resultField, times)
            } else {
                pipeline += lookupPipelineBuilder.toPipeline(lookupWrapper.lookupWrappers)
            }
            if (times == 1)
                resultFieldStack.remove(resultField)
            else
                resultFieldStack[resultField] = times - 1
        } else {
            fixedLookupList(apiFilter)?.find { kProperty2 ->
                val kProperty1 = lookupPipelineBuilder.resultProperty
                val owner1 = kProperty1.instanceParameter?.type?.classifier as? KClass<*> ?: return@find false
                val owner2 = kProperty2.instanceParameter?.type?.classifier as? KClass<*> ?: return@find false
                owner2.isSubclassOf(owner1) && kProperty2.name == kProperty1.name
            }?.let {
                val resultField = ResultField(kResultField = lookupPipelineBuilder.resultProperty)
                val times = resultFieldStack[resultField]?.inc() ?: 1
                resultFieldStack[resultField] = times
                if (times > Coll.MAX_RECURSIVE_RESULT_FIELD) {
                    outErr(resultField, times)
                } else {
                    pipeline += lookupPipelineBuilder.toPipeline()
                }
                if (times == 1)
                    resultFieldStack.remove(resultField)
                else
                    resultFieldStack[resultField] = times - 1
            }
        }
    }
    return pipeline
}

/**
 * Constructs the full aggregation pipeline based on match, sort, lookup, and post-lookup stages.
 *
 * @param call Optional ApplicationCall for request context.
 * @param apiFilter The filter for match/sort/lookup stages.
 * @param apiRequestParams Optional request parameters with remote filters/sorters.
 * @param lookupWrappers Lookup wrappers for nested lookups.
 * @param resultUnit Whether the result is a single item or a list.
 * @return A mutable list of BSON pipeline stages.
 */
internal fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> Coll<*, T, ID, FILT, *>.buildPipeline(
    call: ApplicationCall? = null,
    apiFilter: FILT = commonContainer.apiFilterInstance(),
    apiRequestParams: ApiRequestParams? = null,
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    resultUnit: ResultUnit,
): MutableList<Bson> {

    val pipeline: MutableList<Bson> = mutableListOf()

    // Execute pipeline transformations before any other pipeline stages
    morphingStage(
        call = call,
        pipeline = pipeline,
        apiFilter = apiFilter,
        apiRequestParams = apiRequestParams,
        resultUnit = resultUnit
    )

    val bsonMatches: ApiRequestParams.MatchLists? = apiRequestParams?.bsonMatches(commonContainer)
    val bsonSorters: ApiRequestParams.SortLists? = apiRequestParams?.bsonSorters()

    var bson: Bson = EMPTY_BSON
    matchStage(call = call, apiFilter = apiFilter, resultUnit = resultUnit)?.let {
        if (Document.parse(it.json).isNotEmpty()) bson = it
    }
    // combine matchStage() result with apiRequestParams pre lookup doc
    and(bson, *(bsonMatches?.preMainLookup?.toTypedArray() ?: emptyArray())).also {
        if (Document.parse(it.json).isNotEmpty()) pipeline.add(match(it))
    }

    // sort is only meaningful when a list is collected
    if (resultUnit == ResultUnit.List) {
        bson = EMPTY_BSON
        sortStage(call = call, apiFilter = apiFilter)?.let {
            if (Document.parse(it.json).isNotEmpty()) bson = it
        }
        // combine sortStage() result with apiRequestParams pre sort doc
        document(bson, bsonSorters?.preMainLookup ?: EMPTY_BSON).also {
            if (Document.parse(it.json).isNotEmpty()) pipeline.add(sort(it))
        }
    }

    // build the main lookups stage
    pipeline += buildLookupList(lookupWrappers = lookupWrappers, apiFilter = apiFilter)

    refactorPipeline(
        call = call,
        pipeline = pipeline,
        apiFilter = apiFilter,
        apiRequestParams = apiRequestParams,
        resultUnit = resultUnit
    )

    val postLookupMatchDoc = mutableListOf<Bson>()
    // first, afterLookupMatchStage()
    afterLookupMatchStage(call = call, apiFilter = apiFilter, resultUnit = resultUnit)?.let {
        postLookupMatchDoc += it
    }
    // second, remote matches
    bsonMatches?.postMainLookup?.let { apiReqPostLookupMatch ->
        postLookupMatchDoc += apiReqPostLookupMatch
    }
    and(*postLookupMatchDoc.toTypedArray()).also {
        if (Document.parse(it.json).isNotEmpty()) pipeline += match(it)
    }

    // first, remote sorts
    val bson1: BsonDocument = bsonSorters?.postMainLookup ?: BsonDocument()
    // second, afterLookupSortStage()
    afterLookupSortStage(call = call, apiFilter = apiFilter)?.let {
        val doc = Document.parse(it.json)
        if (doc.isNotEmpty()) {
            doc.forEach { (key, value) ->
                bson1.append(key, BsonInt32(value as Int))
            }
        }
    }
    if (Document.parse(bson1.json).isNotEmpty()) pipeline += sort(bson1)
    return pipeline
}

/**
 * Prints the aggregation pipeline stages for debugging.
 *
 * @param pipeline The pipeline stages to print.
 */
internal fun Coll<*, *, *, *, *>.printOutPipeline(pipeline: List<Bson>) {
    println("-".repeat(40))
    println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate pipeline:")
    println("*".repeat(40))
    println(pipeline.json2)
    println("+".repeat(40))
}

/**
 * Builds and returns an AggregatePublisher with pagination support.
 *
 * @param call Optional ApplicationCall for request context.
 * @param pipeline Initial pipeline stages.
 * @param lookupWrappers Lookup wrappers for nested lookups.
 * @param apiFilter The API filter for match/sort/lookup.
 * @param apiRequestParams Optional pagination/filter parameters.
 * @param countType The counting strategy for pagination.
 * @param resultUnit Whether the result is single or list.
 * @param debug Whether to print the pipeline for debugging.
 * @param pageStateInfoFun Callback to receive pagination info.
 * @return An AggregatePublisher for the constructed pipeline.
 */
internal fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> Coll<*, T, ID, FILT, *>.buildAggregatePublisher(
    call: ApplicationCall? = null,
    pipeline: MutableList<Bson> = mutableListOf(),
    lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    apiFilter: FILT = commonContainer.apiFilterInstance(),
    apiRequestParams: ApiRequestParams? = null,
    countType: CountType = CountType.PreLookup,
    resultUnit: ResultUnit,
    debug: Boolean,
    pageStateInfoFun: ((PageCountInfo) -> Unit)? = null,
): AggregatePublisher<T> {
    pipeline += buildPipeline(
        call = call,
        apiFilter = apiFilter,
        apiRequestParams = apiRequestParams,
        lookupWrappers = lookupWrappers,
        resultUnit = resultUnit
    )
    apiRequestParams?.let { requestParams ->
        val pageCountInfo: PageCountInfo = when (countType) {
            CountType.PreLookup -> PageCountInfo(
                match = matchStage(apiFilter = apiFilter, resultUnit = resultUnit),
                countType = countType
            )

            CountType.PostLookup -> PageCountInfo(
                pipeline = pipeline + Aggregates.count(),
                countType = countType
            )

            CountType.Estimated -> PageCountInfo(countType = countType)
            CountType.Unknown -> PageCountInfo(countType = countType)
        }
        pageStateInfoFun?.invoke(pageCountInfo)
        requestParams.pageSize?.let { pageSize ->
            if (pageSize > 0) {
                (pageSize * ((requestParams.page ?: 1) - 1)).let { skip -> if (skip > 0) pipeline.add(skip(skip)) }
                pipeline.add(limit(pageSize))
            }
        }
    }
    if (debug) {
        printOutPipeline(pipeline)
    }
    return mongoColl.aggregate(pipeline, commonContainer.itemKClass.java)
}
