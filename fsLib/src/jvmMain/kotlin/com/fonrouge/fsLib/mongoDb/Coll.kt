package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.FieldPath
import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.apiData.*
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.base.IAppRole
import com.fonrouge.fsLib.model.base.IAppRole.RoleType
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.property.KPropertyPath
import java.util.*
import kotlin.jvm.internal.PropertyReference1Impl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/**
 * Extension property that retrieves the collection name for a given class extending `BaseDoc`.
 *
 * This property navigates the class hierarchy to find a `@Collection` annotation,
 * and returns the specified collection name. If no `@Collection` annotation is found
 * in the class hierarchy, the simple name of the class is returned. If the class has no
 * simple name (i.e., it is anonymous or synthetic), an empty string is returned.
 */
val KClass<out BaseDoc<*>>.collectionName: String
    get() {
        var self: KClass<*>? = this
        var name: String?
        do {
            name = self?.findAnnotation<Collection>()?.name
            if (name == null) {
                self = self?.superclasses?.firstOrNull()
            }
        } while (name == null && self != Any::class)
        name ?: run { name = simpleName?.let { it.first().lowercase() + it.substring(1) } }
        return name ?: ""
    }

/**
 * Represents a collection (Coll) with various functionalities to handle CRUD operations,
 * aggregation pipelines, and API-based data processing. The class is designed to interact
 * with a MongoDB collection, offering methods for item creation, updates, deletions,
 * and complex list processing with filters, lookups, and debug features.
 *
 * @param commonContainer A container instance holding shared configurations or dependencies.
 * @param debug Flag indicating if debug mode is enabled for detailed logging.
 * @param dependencies A list of dependent data structures and their relationships to this collection.
 * Dependencies represent linked data structures that reference items in this collection through properties,
 * allowing tracking of relationships between collections and prevention of orphaned references.
 * Each dependency specifies the container holding the dependent items and the property that links to this collection.
 * @param coroutine The coroutine context for executing asynchronous tasks.
 * @param lookupFun A lookup function to be used in aggregation pipelines or data processing.
 * @param mongoDatabase The MongoDB database instance associated with this collection.
 * @param mongoColl The MongoDB collection instance being operated upon.
 * @param objName The name of the object or collection represented by this instance.
 * @param readOnly Flag to determine if the collection is read-only (no write operations allowed).
 * @param resultFieldStack A stack for result field tracking or state management.
 * @param readOnlyErrorMsg The error message to display when a write operation is attempted in a read-only mode.
 */
abstract class Coll<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val commonContainer: CC,
    mongoDbBuilder: MongoDbBuilder? = null,
    private var debug: Boolean = false
) {
    companion object {
        private var privateRoleInUserColl: IRoleInUserColl<*, *, *, *, *, *>? = null
        var MAX_RECURSIVE_RESULT_FIELD = 1
    }

    /**
     * Provides a list of dependencies that reference this collection.
     * When a document is deleted, this list is used to check for any existing references
     * to prevent orphaning data in dependent collections.
     *
     * @return A function that returns a list of [Dependency] objects describing the relationships
     * between this collection and other collections that depend on its documents, or null if there
     * are no dependencies
     */
    open val dependencies: (() -> List<Dependency<*, ID>>)? = null

    /**
     * Represents a dependency relationship between collections.
     *
     * @param T The type of document in the dependent collection that references this collection
     * @param ID The type of the identifier used to reference items in this collection
     * @param common The container managing the dependent collection's items
     * @param property The property within the dependent document that references items in this collection
     */
    data class Dependency<T : BaseDoc<*>, ID : Any>(
        val common: ICommonContainer<out T, *, *>,
        val property: KProperty1<out T, ID?>
    )

    /**
     * A coroutine-based collection instance derived from a MongoDB collection.
     * This variable provides coroutine support for asynchronous operations
     * on the MongoDB collection, enabling more efficient and non-blocking database
     * interactions.
     *
     * @param T The type of documents stored within the MongoDB collection.
     */
    val coroutine: CoroutineCollection<T> get() = mongoColl.coroutine

    /**
     * A function that takes a filter of type `FILT` and returns a list of `LookupPipelineBuilder` instances.
     *
     * This function is designed to perform lookups based on the provided filter and build a pipeline of
     * lookup operations. By default, it returns an empty list, indicating no lookups.
     *
     * @param FILT the type of the filter used for lookups.
     * @return a list of `LookupPipelineBuilder` instances based on the provided filter.
     */
    open val lookupFun: (FILT) -> List<LookupPipelineBuilder<T, *, *>> = { listOf() }

    val mongoDatabase: MongoDatabase = mongoDbBuilder?.getMongoDb() ?: com.fonrouge.fsLib.mongoDb.mongoDatabase

    val mongoColl: MongoCollection<T> =
        mongoDatabase.getCollection(
            commonContainer.itemKClass.collectionName,
            commonContainer.itemKClass.java
        )

    val objName: String
        get() = this::class.simpleName ?: run {
            this::class.superclasses[0].simpleName ?: "unknown"
        }

    /**
     * Indicates if the current instance should be read-only.
     *
     * This variable determines whether the instance can be modified or not.
     * If set to `true`, the instance is immutable and cannot be changed.
     * If set to `false`, the instance is mutable and can be altered.
     */
    open val readOnly = false

    /**
     * A mutable map that associates a `ResultField` with an integer value.
     * This map is used to track or store specific relationships or mappings
     * involving `ResultField` objects and their corresponding integer identifiers or counters.
     */
    private val resultFieldStack = mutableMapOf<ResultField, Int>()

    val readOnlyErrorMsg get() = "${commonContainer.labelItem} is read-only"

    /**
     * Handles the creation action for a given API item and inserts it into the data store.
     *
     * @param apiItem The API item containing the data and parameters required for the creation action.
     * @return The state of the created item after the insertion, encapsulated in an ItemState object.
     */
    protected open suspend fun actionCreate(
        apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>
    ): ItemState<T> = insertOne(
        apiItem.copy(item = apiItem.item.copyItemWithPrimaryConstructorParameters())
    )

    /**
     * Executes an update action on the given API item and returns the updated item state.
     *
     * @param apiItem The update action containing the necessary data and filters to perform the update operation.
     * @return The state of the updated item after the operation is completed.
     */
    protected open suspend fun actionUpdate(
        apiItem: ApiItem.Upsert.Update.Action<T, ID, FILT>,
    ): ItemState<T> = updateOne(
        apiItem.copy(
            item = apiItem.item.copyItemWithPrimaryConstructorParameters(),
            orig = apiItem.orig?.copyItemWithPrimaryConstructorParameters()
        )
    )

    /**
     * Executes the delete action for a specific item and returns the resulting state of the item.
     *
     * @param apiItem The configuration object containing the delete action,
     *                including the item to be deleted and related information.
     * @return The state of the item after the delete action has been performed.
     */
    protected open suspend fun actionDelete(
        apiItem: ApiItem.Delete.Action<T, ID, FILT>,
    ): ItemState<T> = deleteOne(apiItem)

    /**
     * Executes a match stage to be performed after a lookup stage in the pipeline.
     *
     * @param apiFilter a filter object containing the conditions or criteria for the lookup match stage.
     * @return a BSON object representing the result of the operation, or null if no processing is performed.
     */
    open fun afterLookupMatchStage(apiFilter: FILT): Bson? = null

    /**
     * Executes a sort stage to be performed after a lookup stage in the pipeline.
     *
     * @param apiFilter the filter criteria to be applied in the lookup or sort stage.
     * @return a BSON object representing the result of the operations, or null if no modifications are made.
     */
    open fun afterLookupSortStage(apiFilter: FILT): Bson? = null

    /**
     * Processes the provided API item based on its type and performs the corresponding
     * CRUD operations or validations.
     *
     * @param iApiItem The API item to be processed.
     * @param call The ApplicationCall context (nullable).
     * @return The resulting state of the item after processing.
     */
    @Suppress("unused")
    suspend fun apiItemProcess(
        iApiItem: IApiItem<T, ID, FILT>,
        call: ApplicationCall?,
    ): ItemState<T> {
        val apiItem: ApiItem<T, ID, FILT> = asApiItem(
            apiItem = iApiItem.asApiItem(commonContainer, call),
        ).let {
            if (it.hasError || it.item == null) {
                return ItemState(isOk = false, msgError = it.msgError)
            } else {
                it.item
            }
        }
        getCrudPermission(
            call = call,
            crudTask = apiItem.crudTask,
        ).also {
            if (it.state == State.Error) return ItemState(it)
        }
        return when (apiItem) {
            is ApiItem.Upsert -> {
                if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
                onPermissionUpsert(apiItem).also { if (it.hasError) return it.asItemState() }
                when (apiItem) {
                    is ApiItem.Upsert.Create -> when (apiItem) {
                        is ApiItem.Upsert.Create.Query -> {
                            onPermissionUpsertCreate(apiItem = apiItem).also { if (it.hasError) return it.asItemState() }
                            queryCreate(apiItem = apiItem)
                        }

                        is ApiItem.Upsert.Create.Action -> actionCreate(apiItem = apiItem)
                    }

                    is ApiItem.Upsert.Update -> when (apiItem) {
                        is ApiItem.Upsert.Update.Query -> {
                            val itemState = findItemState(apiItem)
                            val item = itemState.item
                            if (itemState.hasError || item == null) return itemState
                            onPermissionUpsertUpdate(
                                apiItem = apiItem,
                                item = item
                            ).also { if (it.hasError) return it.asItemState() }
                            queryUpdate(apiItem = apiItem, item = item)
                        }

                        is ApiItem.Upsert.Update.Action -> actionUpdate(apiItem = apiItem)
                    }
                }
            }

            is ApiItem.Read -> {
                val itemState = findItemStateById(id = apiItem.id)
                onPermissionRead(apiItem = apiItem).also { if (it.hasError) return it.asItemState() }
                val item = itemState.item
                if (itemState.hasError || item == null) return itemState
                queryRead(apiItem = apiItem, item = item)
            }

            is ApiItem.Delete -> {
                if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
                when (apiItem) {
                    is ApiItem.Delete.Query -> {
                        val itemState = findItemStateById(apiItem.id)
                        val item = itemState.item
                        if (itemState.hasError || item == null) return itemState
                        findChildrenNot(item).also { if (it.hasError) return it }
                        onPermissionDelete(
                            apiItem = apiItem,
                            item = item
                        ).also { if (it.hasError) return it.asItemState() }
                        queryDelete(apiItem = apiItem, item = item)
                    }

                    is ApiItem.Delete.Action -> {
                        findChildrenNot(apiItem.item).also { if (it.hasError) return it }
                        onPermissionDelete(
                            apiItem = apiItem,
                            item = apiItem.item
                        ).also { if (it.hasError) return it.asItemState() }
                        actionDelete(apiItem = apiItem)
                    }
                }
            }
        }
    }

    /**
     * Converts the provided `ApiItem` into an `ItemState` containing the given `ApiItem` instance.
     *
     * @param apiItem The API item to be wrapped in an ItemState.
     * @return An ItemState object that contains the given ApiItem.
     */
    open suspend fun asApiItem(
        apiItem: ApiItem<T, ID, FILT>,
    ): ItemState<ApiItem<T, ID, FILT>> = ItemState(item = apiItem)

    /**
     * Constructs and executes an aggregation pipeline to generate an `AggregatePublisher` result based on the
     * provided parameters like filter, pagination, custom lookups, and debug settings.
     *
     * @param pipeline The initial pipeline to use. Defaults to an empty mutable list.
     * @param lookupWrappers A list of custom lookup wrappers to include in the aggregation pipeline. Defaults to an empty list.
     * @param apiFilter The API filter instance to apply over the aggregation pipeline. Defaults to a common container filter instance.
     * @param apiRequestParams Request parameters for API calls, including pagination details. Can be null.
     * @param countType Specifies the type of count operation to use, such as `PreLookup` or `PostLookup`. Default is `CountType.PreLookup`.
     * @param debug Flag to enable or disable debug information for the aggregation pipeline execution. Defaults to the class-level debug configuration.
     * @param pageStateInfoFun A callback function that provides `PageCountInfo` details based on the count type and pipeline state. Can be null.
     * @return An `AggregatePublisher` containing the result of executing the aggregation pipeline.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun aggregateLookupPublisher(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        apiRequestParams: ApiRequestParams? = null,
        countType: CountType = CountType.PreLookup,
        resultUnit: ResultUnit,
        debug: Boolean = this.debug,
        pageStateInfoFun: ((PageCountInfo) -> Unit)? = null,
    ): AggregatePublisher<T> {
        pipeline += pipeline(
            apiFilter = apiFilter,
            apiRequestParams = apiRequestParams,
            lookupWrappers = lookupWrappers,
            resultUnit = resultUnit
        )
        apiRequestParams?.let { requestParams ->
            val pageCountInfo: PageCountInfo = when (countType) {
                CountType.PreLookup -> PageCountInfo(
                    match = matchStage(apiFilter),
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

    /**
     * Performs an aggregation operation on a MongoDB collection using the specified pipeline,
     * lookup wrappers, and API filter, and returns an `AggregatePublisher` for further processing.
     *
     * @param pipeline Optional list of aggregation stages represented as a list of `Bson`.
     *                 If null, a default pipeline is generated based on the `apiFilter`, `lookupWrappers`, and `ResultUnit.Single`.
     * @param lookupWrappers A list of `LookupWrapper` instances to specify lookup operations during aggregation. Defaults to an empty list.
     * @param apiFilter An instance of `FILT` used to apply specific filters as part of the aggregation. Defaults to the common container's API filter instance.
     * @return An `AggregatePublisher` representing the result of the aggregation operation.
     */
    @Suppress("unused")
    fun aggregateSingleLookup(
        pipeline: List<Bson>? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
    ): AggregatePublisher<T> {
        val pipeline1 = pipeline ?: pipeline(
            apiFilter = apiFilter,
            lookupWrappers = lookupWrappers,
            resultUnit = ResultUnit.Single
        )
        if (debug) {
            printOutPipeline(pipeline1)
        }
        return mongoColl.aggregate(pipeline1, commonContainer.itemKClass.java)
    }

    /**
     * Processes a list for an API call, applying various stages, lookups, filters, and post-processing steps.
     *
     * @param call Optional ApplicationCall that might be used to fetch user session information.
     * @param apiRequestParams The initial stage of the list processing pipeline.
     * @param lookupWrappers A list of LookupWrapper instances for performing lookup operations in the pipeline.
     * @param apiFilter An instance of a filter to be applied to the API list.
     * @param countType Specifies the type of count operation to be performed.
     * @param debug Optional debug flag to control debug output.
     * @param postProcessList Optional function to further process the list after retrieval.
     * @return ListState containing the processed list data, pagination information, and state status.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun apiListProcess(
        call: ApplicationCall? = null,
        apiRequestParams: ApiRequestParams,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        countType: CountType = CountType.PreLookup,
        debug: Boolean = this.debug,
        postProcessList: ((List<T>) -> List<T>)? = null,
    ): ListState<T> {
        call?.let { privateRoleInUserColl?.let { call.sessions.get(it.userKClass) } }?.let {
            getCrudPermission(
                call = call,
                crudTask = CrudTask.Read,
            ).also {
                if (it.hasError) return ListState(state = State.Error, msgError = "User not authorized")
            }
        }
        var pageCountInfo: PageCountInfo? = null
        val publisher = aggregateLookupPublisher(
            lookupWrappers = lookupWrappers,
            apiFilter = apiFilter,
            apiRequestParams = apiRequestParams,
            countType = countType,
            debug = debug,
            pageStateInfoFun = {
                pageCountInfo = it
            },
            resultUnit = ResultUnit.List,
        )
        val curTime = Date().time
        var t1: Long? = null
        var t2: Long? = null
        var list: List<T> = emptyList()
        coroutineScope {
            val j1 = launch {
                list = publisher.toList()
                t1 = Date().time - curTime
            }
            val j2 = launch {
                apiRequestParams.pageSize?.let {
                    pageCountInfo?.count(this@Coll, apiRequestParams.pageSize)
                }
                t2 = Date().time - curTime
            }
            joinAll(j1, j2)
        }
        if (debug) {
            println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate time: ${t1}ms, Count time: ${t2}ms")
        }
        return ListState(
            data = postProcessList?.invoke(list) ?: list,
            last_page = pageCountInfo?.lastPage,
            last_row = pageCountInfo?.lastRow,
            state = State.Ok,
        )
    }

    /**
     * Processes a list for API responses with configurable match, sort, filter, and post-process stages.
     *
     * @param call Optional `ApplicationCall` context for the current API request, used for request details.
     * @param apiList The `ApiList` object containing configuration for pagination, filtering, sorting, and API-specific filters.
     * @param countType Specifies the type of count operation to be performed, either pre-lookup or post-lookup.
     * @param debug Optional flag to enable or disable debug mode for logging or tracing the operation.
     * @param lookupWrappers A list of `LookupWrapper` objects to be applied in the lookup pipeline.
     * @param postProcessList Optional lambda function to post-process the resulting list after fetching data.
     * @return A `ListState` object containing the processed list along with related metadata such as pagination information.
     */
    @Suppress("unused")
    suspend fun apiListProcess(
        call: ApplicationCall? = null,
        apiList: ApiList<FILT>,
        countType: CountType = CountType.PreLookup,
        debug: Boolean = this.debug,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        postProcessList: ((List<T>) -> List<T>)? = null
    ): ListState<T> {
        return apiListProcess(
            call = call,
            apiRequestParams = ApiRequestParams(
                page = apiList.tabPage,
                pageSize = apiList.tabSize,
                remoteFilters = apiList.tabFilter,
                remoteSorters = apiList.tabSorter,
            ),
            lookupWrappers = lookupWrappers,
            apiFilter = apiList.apiFilter,
            countType = countType,
            debug = debug,
            postProcessList = postProcessList
        )
    }

    /**
     * Builds a lookup pipeline list based on the provided lookup wrappers and API filter.
     *
     * @param lookupWrappers A list of `LookupWrapper` instances that define the lookup configurations. Defaults to an empty list if not provided.
     * @param apiFilter The API filter instance used to determine the lookup functions and pipelines.
     * @return A mutable list of `Bson` representing the pipeline to be applied for the lookup operation.
     */
    private fun buildLookupList(
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
            val lookupWrapper: LookupWrapper<*, *>? = lookupWrappers.find { lookupWrapper ->
                val kProperty1 = lookupPipelineBuilder.resultProperty
                val owner1 = kProperty1.instanceParameter?.type?.classifier as? KClass<*> ?: return@find false
                when (lookupWrapper) {
                    is LookupByProperty<*, *> -> lookupWrapper.resultProperty //as PropertyReference1Impl
                    is LookupByPipeline<*, *, *> -> lookupWrapper.pipeline.resultProperty as PropertyReference1Impl
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
                if (times > MAX_RECURSIVE_RESULT_FIELD) {
                    outErr(resultField, times)
                } else {
                    pipeline += lookupPipelineBuilder.toPipeline(lookupWrapper.lookupWrappers)
                }
                if (times == 1)
                    resultFieldStack.remove(resultField)
                else
                    resultFieldStack[resultField] = times - 1
            } else {
                fixedLookupList(apiFilter)?.find { kProperty1 -> kProperty1 == lookupPipelineBuilder.resultProperty }
                    ?.let {
                        val resultField = ResultField(kResultField = lookupPipelineBuilder.resultProperty)
                        val times = resultFieldStack[resultField]?.inc() ?: 1
                        resultFieldStack[resultField] = times
                        if (times > MAX_RECURSIVE_RESULT_FIELD) {
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

    private data class ResultField(
        val threadId: Long = Thread.currentThread().threadId(),
        val kResultField: KProperty1<*, *>
    )

    /**
     * Performs a bulk write operation asynchronously on the provided list of write models.
     *
     * @param writeModels A mutable list of write models to be written in bulk.
     * @param debug A boolean flag indicating whether to print debug information. Default is false.
     */
    @Suppress("unused")
    fun bulkWrite(writeModels: MutableList<WriteModel<T>>, debug: Boolean = false) {
        CoroutineScope(context = Dispatchers.IO).launch {
            if (writeModels.isNotEmpty()) {
                if (debug) {
                    println("BulkWrite ${writeModels.hashCode()} start with ${writeModels.size} items.")
                }
                val r = coroutine.bulkWrite(writeModels)
                if (debug) {
                    println("BulkWrite ${writeModels.hashCode()} result = ${r.insertedCount}")
                }
                writeModels.clear()
            }
        }
    }

    /**
     * Creates a copy of the current item by invoking its primary constructor with the specified field assignments
     * or the current values of the item's member properties.
     *
     * @param fieldAssignments A list of field assignments providing specific values to set for fields during copying.
     *                          If not provided, defaults to an empty list, and the current values of the item's properties are used.
     * @return A new instance of the item, created using its primary constructor with the specified or current values.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun T.copyItemWithPrimaryConstructorParameters(
        vararg fieldAssignments: AssignTo<T, *> = emptyArray()
    ): T {
        val mp = commonContainer.itemKClass.memberProperties.associateBy { it.name }
        val cp = commonContainer.itemKClass.primaryConstructor?.parameters?.mapNotNull { it.name } ?: emptyList()
        val o = fieldAssignments.associate { it.kField.name to it.value }
        val values = cp.map { o.getOrElse(it) { mp[it]?.get(this) } }
        return commonContainer.itemKClass.primaryConstructor?.call(*values.toTypedArray())
            ?: commonContainer.itemKClass.createInstance() //throw Exception("Unable to copy item")
    }

    /**
     * Deletes a single item from the collection based on the specified ID and an optional filter.
     *
     * @param id The unique identifier for the item to be deleted.
     * @param filter An optional BSON filter to further specify which item to delete.
     * @return The state of the item after the delete operation, encapsulated in an ItemState object.
     */
    @Suppress("unused")
    suspend fun deleteOne(
        id: ID,
        filter: Bson? = null,
    ): ItemState<T> {
        return deleteOne(
            apiItem = ApiItem.Delete.Action(
                item = coroutine.findOneById(id) ?: return ItemState(),
                apiFilter = commonContainer.apiFilterInstance(),
            ),
            filter = filter
        )
    }

    /**
     * Deletes a single item from the database based on the provided filter.
     *
     * @param apiItem The API item representing the action to perform the delete operation.
     * @param filter An optional filter to apply to the delete operation.
     *
     * @return The state of the delete operation. It contains information about whether the operation was successful or not,
     * as well as any error message in case of failure.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteOne(
        apiItem: ApiItem.Delete.Action<T, ID, FILT>,
        filter: Bson? = null,
    ): ItemState<T> {
        if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
        findChildrenNot(apiItem.item).also { if (it.hasError) return it }
        onPermissionDelete(apiItem = apiItem, item = apiItem.item).also { if (it.hasError) return it.asItemState() }
        onBeforeDeleteAction(apiItem = apiItem).also { if (it.hasError) return it }
        var result: Boolean? = null
        return try {
            result = coroutine.deleteOne(
                and(
                    BaseDoc<*>::_id eq apiItem.item._id,
                    filter ?: EMPTY_BSON
                )
            ).deletedCount == 1L
            ItemState(
                isOk = result,
                msgOk = "Delete operation ok"
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        } finally {
            onAfterDeleteAction(apiItem = apiItem, result = result == true)
        }
    }

    /**
     * Finds an entity by its ID with optional filters, API filter, and lookup wrappers.
     *
     * @param id The ID of the entity to find.
     * @param filter Optional Bson filter to apply additional query conditions.
     * @param apiFilter The API filter instance to apply to the query.
     * @param lookupWrappers List of LookupWrapper instances to specify lookup conditions for related collections.
     * @return The entity matching the provided ID and filters, or null if not found.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findById(
        id: ID?,
        filter: Bson? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    ): T? {
        return findOne(and(BaseDoc<*>::_id eq id, filter), apiFilter, lookupWrappers)
    }

    /**
     * Finds and checks child dependencies of the given item and determines if they are not valid.
     *
     * @param item The item of type T whose children dependencies are to be checked.
     * @return An [ItemState] representing the state of the item. It may include errors if any
     *         invalid child dependencies are detected.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findChildrenNot(
        item: T,
    ): ItemState<T> {
        val itemState = findItemStateById(item._id)
        if (itemState.hasError.not()) {
            dependencies?.invoke()?.forEach { dependency ->
                val kProperty1: KProperty1<out BaseDoc<*>, ID?> = dependency.property
                when (kProperty1) {
                    is FieldPath -> kProperty1.path to kProperty1.owner.collectionName
                    is PropertyReference1Impl -> {
                        @Suppress("UNCHECKED_CAST")
                        kProperty1.name to (kProperty1.owner as KClass<out BaseDoc<*>>).collectionName
                    }

                    is KPropertyPath<*, ID?> -> return ItemState(
                        state = State.Error,
                        msgError = "Child field path '${commonContainer.itemKClass.simpleName}::${kProperty1.path()}' not valid (if you used '/' or '%' operators you must use '+' operator)"
                    )

                    else -> null
                }?.let { (fieldName: String, collectionName) ->
                    mongoDatabase.getCollection(collectionName).also { mongoCollection ->
                        mongoCollection.coroutine.find(Document(fieldName, item._id)).first()?.let {
                            return ItemState(
                                state = State.Error,
                                msgError = "'${commonContainer.labelItemId(item)}' ${("tiene dependencias en")} '${dependency.common.labelList}'"
                            )
                        }
                    }
                }
            }
        }
        return itemState
    }

    /**
     * Filters items based on the criteria specified in the given filter.
     *
     * @param apiFilter the filter criteria to apply when searching for items.
     * @return a BSON object representing the filter to be applied, or null if no filter is specified.
     */
    open fun findItemFilter(apiFilter: FILT): Bson? = null

    /**
     * Finds the state of an item based on the provided API query and lookup wrappers.
     *
     * @param apiItem An instance of ApiItem.Query containing the item's ID and filter.
     * @param lookupWrappers An optional list of LookupWrapper instances to perform additional lookups.
     * @return The state of the item as an ItemState instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findItemState(
        apiItem: ApiItem<T, ID, FILT>,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList()
    ): ItemState<T> {
        val id: ID = when (apiItem) {
            is ApiItem.Upsert.Create.Query -> null
            is ApiItem.Upsert.Create.Action -> apiItem.item._id
            is ApiItem.Read -> apiItem.id
            is ApiItem.Upsert.Update.Query -> apiItem.id
            is ApiItem.Upsert.Update.Action -> apiItem.item._id
            is ApiItem.Delete.Query -> apiItem.id
            is ApiItem.Delete.Action -> apiItem.item._id
        } ?: return ItemState(isOk = false)
        return findItemStateById(
            id = id,
            apiFilter = apiItem.apiFilter,
            lookupWrappers = lookupWrappers,
        )
    }

    /**
     * Finds the state of an item by its identifier.
     *
     * @param id The identifier of the item.
     * @param apiFilter The API filter to be applied; defaults to commonContainer's API filter instance.
     * @param filter The BSON filter for querying the item; defaults to the result of the `findItemFilter` function.
     * @param lookupWrappers List of lookup wrappers to be used in the query; defaults to an empty list.
     * @return An [ItemState] containing the item or an error message if the item could not be found.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findItemStateById(
        id: ID?,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        filter: Bson? = findItemFilter(apiFilter),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList()
    ): ItemState<T> {
        return try {
            ItemState(
                item = findById(id = id, filter = filter, apiFilter = apiFilter, lookupWrappers = lookupWrappers),
                msgError = "_id '$id' (${commonContainer.itemKClass.simpleName}) not found..."
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
    }

    /**
     * Suspends the current coroutine and performs a query to find a list of documents from the database
     * based on the provided filter, lookup wrappers, and api filter.
     *
     * @param filter Optional Bson filter to apply to the query. Defaults to null.
     * @param lookupWrappers List of LookupWrapper instances to be used for lookups in the query. Defaults to an empty list.
     * @param apiFilter Instance of FILT used for additional API-level filtering. Defaults to an instance from the commonContainer.
     * @param debug Boolean flag to enable or disable debugging for the query. Defaults to false.
     * @return A list of documents of type T matching the query criteria.
     */
    @Suppress("unused")
    suspend fun findList(
        filter: Bson? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        debug: Boolean = this.debug,
    ): List<T> {
        return findPublisher(
            filter = filter,
            lookupWrappers = lookupWrappers,
            apiFilter = apiFilter,
            resultUnit = ResultUnit.List,
            debug = debug,
        ).toList()
    }

    /**
     * Finds a single document based on the provided filter criteria and lookup wrappers.
     *
     * @param filter BSON filter to narrow down the search.
     * @param apiFilter An instance of FILT used to apply additional API-level filters.
     * @param lookupWrappers List of LookupWrapper instances for join operations.
     * @param debug Boolean flag to enable or disable debug mode.
     * @return The first document matching the filter criteria or null if no match is found.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOne(
        filter: Bson? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        debug: Boolean = false,
    ): T? {
        return findPublisher(
            filter = filter,
            lookupWrappers = lookupWrappers,
            apiFilter = apiFilter,
            resultUnit = ResultUnit.Single,
            debug = debug,
        ).awaitFirstOrNull()
    }

    /**
     * Finds a publisher with the specified criteria.
     *
     * @param filter Filter condition in BSON format. Defaults to null.
     * @param lookupWrappers List of lookup wrappers to be applied. Defaults to an empty list.
     * @param apiFilter An instance of the API filter. Defaults to `commonContainer.apiFilterInstance()`.
     * @param debug Flag to enable or disable debug mode. Defaults to false.
     * @return An instance of [AggregatePublisher] that runs the aggregate query with the given criteria.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun findPublisher(
        filter: Bson? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        resultUnit: ResultUnit,
        debug: Boolean = false,
    ): AggregatePublisher<T> {
        return aggregateLookupPublisher(
            pipeline = filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(),
            lookupWrappers = lookupWrappers,
            apiFilter = apiFilter,
            resultUnit = resultUnit,
            debug = debug,
        )
    }

    /**
     * Generates a fixed lookup list based on the provided API filter.
     *
     * @param apiFilter An optional filter parameter used to control or refine the lookup process.
     * By default, it retrieves the instance of the API filter from the common container.
     * @return A list of KProperty1 instances or null, representing the properties of the type `T`
     * that match the criteria defined by the provided API filter.
     */
    open fun fixedLookupList(
        apiFilter: FILT = commonContainer.apiFilterInstance()
    ): List<KProperty1<in T, *>>? = null

    /**
     * Determines the CRUD (Create, Read, Update, Delete) permission for a given user.
     *
     * @param call The ApplicationCall associated with the request, which may contain session information.
     * @param crudTask The specific CRUD task for which permission is being checked.
     * @return A SimpleState indicating whether the permission check was successful, including an error message if not.
     */
    suspend fun getCrudPermission(
        call: ApplicationCall?,
        crudTask: CrudTask,
    ): SimpleState {
        val user: IUser<*>? = privateRoleInUserColl?.let { call?.sessions?.get(it.userKClass) }
        val roleInUserColl = privateRoleInUserColl ?: return SimpleState(isOk = true)
        if (user == null) return SimpleState(isOk = false, msgError = "Empty user.")
        val matchDoc = and(
            IAppRole<*>::roleType eq RoleType.CrudTask,
            IAppRole<*>::classOwner eq commonContainer.name
        )
        return roleInUserColl.permissionState(
            roleType = RoleType.CrudTask,
            user = user,
            crudTask = crudTask
        ) {
            roleInUserColl.appRoleColl.findOne(matchDoc)?.let {
                ItemState(item = it)
            } ?: roleInUserColl.appRoleColl.insertCrudRole(
                container = commonContainer,
                crudTask = crudTask
            ).item?.let {
                ItemState(item = it)
            } ?: ItemState(
                isOk = false,
                msgError = "App role doesn't exist '${commonContainer.name}' for ${commonContainer.labelItem} item."
            )
        }
    }

    /**
     * Retrieves the indexes of the documents within the collection.
     *
     * This function is a coroutine and should be called within a coroutine scope.
     *
     * @receiver CoroutineCollection<T> The collection from which indexes are obtained.
     */
    open suspend fun CoroutineCollection<T>.indexes() {}

    /**
     * Inserts a single item into the database or dataset.
     *
     * @param item The item to be inserted.
     * @param apiFilter The filter to be applied during the insertion process. Defaults to a common container filter instance.
     * @return The state of the item after the insertion, encapsulated in an ItemState object.
     */
    @Suppress("unused")
    suspend fun insertOne(
        item: T,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        call: ApplicationCall? = null,
    ): ItemState<T> = insertOne(
        apiItem = ApiItem.Upsert.Create.Action(
            item = item,
            apiFilter = apiFilter,
            call = call,
        ),
    )

    /**
     * Inserts a single item into the data store.
     *
     * @param apiItem The API item containing the query and filter information for the insert operation.
     * @param item The item to be inserted.
     * @return The state of the item after the insert operation, with a flag indicating if the item was already present.
     */
    @Suppress("unused")
    suspend fun insertOne(
        apiItem: ApiItem.Upsert.Create.Query<T, ID, FILT>,
        item: T,
    ): ItemState<T> {
        val itemState = insertOne(
            apiItem = ApiItem.Upsert.Create.Action(item = item, apiFilter = apiItem.apiFilter),
        )
        return itemState.copy(itemAlreadyOn = true)
    }

    /**
     * Inserts a single item into the collection.
     *
     * @param apiItem The item to be inserted, wrapped in an Upsert.Create.Action object.
     * @return An ItemState indicating the result of the insertion.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun insertOne(
        apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>,
    ): ItemState<T> {
        if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
        onPermissionUpsert(apiItem).also { if (it.hasError) return it.asItemState() }
        onPermissionUpsertCreate(apiItem).also { if (it.hasError) return it.asItemState() }
        var apiItem1 = apiItem.copy(item = apiItem.item.copyItemWithPrimaryConstructorParameters())
        onBeforeUpsertAction(apiItem1).also { it ->
            if (it.hasError) return it
            it.item?.let { apiItem1 = apiItem1.copy(item = it.copyItemWithPrimaryConstructorParameters()) }
        }
        onBeforeUpsertCreateAction(apiItem1).also { it ->
            if (it.hasError) return it
            it.item?.let { apiItem1 = apiItem1.copy(item = it.copyItemWithPrimaryConstructorParameters()) }
        }
        var result: Boolean? = null
        return try {
            apiItem1 = apiItem1.copy(item = apiItem1.item.copyItemWithPrimaryConstructorParameters())
            onValidate(apiItem1, apiItem1.item).also { if (it.hasError) return it.asItemState() }
            val insertOneResult: InsertOneResult = mongoColl.insertOne(
                apiItem1.item,
            ).awaitSingle()
            result = insertOneResult.insertedId != null
            ItemState(item = apiItem1.item, state = if (result) State.Ok else State.Error)
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        } finally {
            onAfterUpsertCreateAction(apiItem = apiItem1, result = result == true)
            onAfterUpsertAction(apiItem = apiItem1, result = result == true)
        }
    }

    /**
     * Builds a BSON representation of the given filter to be used in a MongoDB aggregation match stage.
     *
     * @param apiFilter The filter of type FILT used to construct the match stage.
     * @return A BSON object representing the match stage, or null if the filter could not be processed.
     */
    open fun matchStage(apiFilter: FILT): Bson? = null

    /**
     * Transforms data in the pipeline before lookups are created, allowing aggregation and restructuring
     * of records. For example, can convert individual sales records into summarized sales reports
     * or reshape document structure. An typical application is using a mongodb group stage or a projection of
     * resulting fields, or adding new calculated fields.
     *
     * @param pipeline The mutable list of BSON objects representing the pipeline that needs to be processed.
     * @param apiFilter The filter instance to be applied during the morphing stage. Defaults to a common container's API filter instance.
     * @param apiRequestParams The parameters of the API request used for modifying or adapting the pipeline.
     * @param resultUnit The resulting unit specifying the output or transformation target of the morphing process.
     */
    open fun morphingStage(
        pipeline: MutableList<Bson>,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        apiRequestParams: ApiRequestParams?,
        resultUnit: ResultUnit,
    ) {
    }

    /**
     * This method is executed after a delete action is performed.
     *
     * @param apiItem Represents the API item on which the delete action was performed, containing details about the action.
     * @param result The result of the delete action, where true indicates success and false indicates failure.
     */
    open suspend fun onAfterDeleteAction(
        apiItem: ApiItem.Delete.Action<T, ID, FILT>,
        result: Boolean
    ) = Unit

    /**
     * Function to be called immediately after an entity is opened.
     * This is a suspend function, allowing for asynchronous operations.
     *
     * Can be overridden to provide specific behavior upon opening.
     */
    open suspend fun onAfterOpen() = Unit

    /**
     * This method is executed after the upsert action is performed.
     *
     * @param apiItem The item that was involved in the upsert action containing
     *                Upsert details such as the type T, identifier ID, and filters FILT.
     * @param result  The result of the upsert action as a Boolean value.
     */
    open suspend fun onAfterUpsertAction(
        apiItem: ApiItem.Upsert<T, ID, FILT>,
        result: Boolean
    ) = Unit

    /**
     * This method is invoked after the "upsert create" action takes place.
     *
     * @param apiItem the API item that represents the "upsert create" action.
     * @param result a boolean indicating the success or failure of the action.
     */
    open suspend fun onAfterUpsertCreateAction(
        apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>,
        result: Boolean
    ) = Unit

    /**
     * This method is called after an upsert update action is performed, allowing for any necessary
     * post-processing or logging based on the result of the action.
     *
     * @param apiItem  The upsert update action that was performed. This includes the specific
     *                 update operation and relevant parameters.
     * @param result   A boolean indicating whether the upsert update action was successful.
     */
    open suspend fun onAfterUpsertUpdateAction(
        apiItem: ApiItem.Upsert.Update.Action<T, ID, FILT>,
        result: Boolean
    ) = Unit

    open suspend fun onBeforeDeleteAction(apiItem: ApiItem.Delete.Action<T, ID, FILT>): ItemState<T> =
        ItemState(isOk = true)

    /**
     * This method is invoked before an upsert operation is performed. It allows for custom processing
     * or validation on the input item and provides a mechanism to prevent the upsert operation if necessary.
     *
     * @param apiItem The upsert operation request containing the item data, its ID, and any applied filters.
     * @return An instance of [ItemState] indicating whether the upsert operation should proceed (`isOk` set to true)
     *         or be halted (`isOk` set to false).
     */
    open suspend fun onBeforeUpsertAction(apiItem: ApiItem.Upsert<T, ID, FILT>): ItemState<T> = ItemState(isOk = true)

    /**
     * This method is called before the upsert create action.
     * It allows for custom pre-processing of the upsert create action.
     *
     * @param apiItem The ApiItem containing the upsert create action details, including the item and its associated metadata.
     * @return The resulting state of the item after any necessary transformations or validations.
     */
    open suspend fun onBeforeUpsertCreateAction(apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>): ItemState<T> =
        ItemState(isOk = true)

    /**
     * This method is called before performing an upsert update action. It allows
     * for any necessary preprocessing or validation of the update action.
     *
     * @param apiItem The update action item containing the item to be updated and related metadata.
     * @return The state of the item after the preprocessing or validation step.
     */
    open suspend fun onBeforeUpsertUpdateAction(apiItem: ApiItem.Upsert.Update.Action<T, ID, FILT>): ItemState<T> =
        ItemState(isOk = true)

    /**
     * Handles the logic to execute when a delete permission is triggered for a specific item.
     *
     * @param apiItem The API item containing delete information and filters.
     * @param item The item that the delete permission affects.
     * @return The state of the item after the delete operation.
     */
    open suspend fun onPermissionDelete(apiItem: ApiItem.Delete<T, ID, FILT>, item: T): SimpleState =
        SimpleState(isOk = true)

    /**
     * Handles the read permission check for the given API item.
     *
     * @param apiItem The API item for which the read permission is being checked.
     * It contains the item details and the ID.
     * @return The current state of the item after checking the read permission.
     */
    open suspend fun onPermissionRead(apiItem: ApiItem.Read<T, ID, FILT>): SimpleState =
        SimpleState(isOk = true)

    /**
     * Handles the upsert operation for the given permission.
     *
     * @param apiItem The API item representing the upsert operation, which includes the item
     * itself, its identifier, and the filter criteria.
     * @return The state of the item after the upsert operation, containing whether the operation
     * was successful.
     */
    open suspend fun onPermissionUpsert(apiItem: ApiItem.Upsert<T, ID, FILT>): SimpleState = SimpleState(isOk = true)

    /**
     * Handles the creation or update of a permission in the system.
     *
     * @param apiItem The entity that contains the data required for creating or updating the permission.
     * @return The state of the item after the upsert operation.
     */
    open suspend fun onPermissionUpsertCreate(apiItem: ApiItem.Upsert.Create<T, ID, FILT>): SimpleState =
        SimpleState(isOk = true)

    /**
     * Handles the update operation for a given item with permissions.
     *
     * @param apiItem The API item update operation containing necessary information.
     * @param item The item to be updated.
     * @return The state of the item after the update operation.
     */
    open suspend fun onPermissionUpsertUpdate(apiItem: ApiItem.Upsert.Update<T, ID, FILT>, item: T): SimpleState =
        SimpleState(isOk = true)

    /**
     * Validates the provided item based on the given API item context.
     *
     * @param apiItem The API item context containing configuration and rules for validation.
     * @param item The item of type T that needs to be validated.
     * @return A SimpleState object indicating the validation result, with isOk set to true if valid.
     */
    open suspend fun onValidate(apiItem: ApiItem.Upsert<T, ID, FILT>, item: T): SimpleState = SimpleState(isOk = true)

    /**
     * Constructs and modifies a MongoDB aggregation pipeline based on class-defined match and sort stages.
     *
     * @param apiFilter The filter object used to determine match and sort stages. Defaults to the common container's API filter instance.
     * @param apiRequestParams Optional request parameters that may include post-lookup match conditions.
     * @param lookupWrappers A list of lookup wrapper objects used to build lookup stages. Defaults to an empty list.
     * @param resultUnit Specifies the result-related configurations to refine or transform the pipeline.
     * @return A mutable list of Bson objects representing the constructed and refined aggregation pipeline.
     */
    fun pipeline(
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        apiRequestParams: ApiRequestParams? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        resultUnit: ResultUnit,
    ): MutableList<Bson> {

        val pipeline: MutableList<Bson> = mutableListOf()

        // Execute pipeline transformations before any other pipeline stages
        morphingStage(
            pipeline = pipeline,
            apiFilter = apiFilter,
            apiRequestParams = apiRequestParams,
            resultUnit = resultUnit
        )

        val bsonMatches: ApiRequestParams.MatchLists? = apiRequestParams?.bsonMatches(commonContainer)
        val bsonSorters: ApiRequestParams.SortLists? = apiRequestParams?.bsonSorters()

        var bson: Bson = EMPTY_BSON
        matchStage(apiFilter)?.let {
            if (Document.parse(it.json).isNotEmpty()) bson = it
        }
        // combine matchStage() result with apiRequestParams pre lookup doc
        and(bson, *(bsonMatches?.preMainLookup?.toTypedArray() ?: emptyArray())).also {
            if (Document.parse(it.json).isNotEmpty()) pipeline.add(match(it))
        }

        bson = EMPTY_BSON
        sortStage(apiFilter)?.let {
            if (Document.parse(it.json).isNotEmpty()) bson = it
        }
        // combine sortStage() result with apiRequestParams pre sort doc
        document(bson, bsonSorters?.preMainLookup ?: EMPTY_BSON).also {
            if (Document.parse(it.json).isNotEmpty()) pipeline.add(sort(it))
        }

        // build the main lookups stage
        pipeline += buildLookupList(lookupWrappers = lookupWrappers, apiFilter = apiFilter)

        refactorPipeline(
            pipeline = pipeline,
            apiFilter = apiFilter,
            apiRequestParams = apiRequestParams,
            resultUnit = resultUnit
        )

        val postLookupMatchDoc = mutableListOf<Bson>()
        // first, afterLookupMatchStage()
        afterLookupMatchStage(apiFilter)?.let {
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
        afterLookupSortStage(apiFilter)?.let {
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
     * Constructs a MongoDB aggregation pipeline based on the provided parameters.
     *
     * @param apiFilter an instance of the filter to apply to the pipeline, defaulting to a common filter.
     * @param matchStage an optional `Bson` match stage to add to the pipeline, or null if no match is required.
     * @param sortStage an optional `Bson` sort stage to apply to the pipeline, or null if no sorting is required.
     * @param lookupWrappers a list of `LookupWrapper` objects defining lookup stages to include in the pipeline, defaults to an empty list.
     * @param refactor an optional lambda function that takes the constructed pipeline as input and allows modification or replacement of it. Returns the final pipeline list.
     * @return a list of `Bson` objects representing the constructed MongoDB aggregation pipeline.
     */
    @Suppress("unused")
    fun pipelineByHand(
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        matchStage: Bson? = null,
        sortStage: Bson? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        refactor: ((MutableList<Bson>) -> List<Bson>)? = null,
    ): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        matchStage?.let { pipeline += match(it) }
        sortStage?.let { pipeline += sort(it) }
        pipeline += buildLookupList(lookupWrappers = lookupWrappers, apiFilter = apiFilter)
        return refactor?.invoke(pipeline) ?: pipeline
    }

    /**
     * Prints out the details of the aggregate pipeline for a specific collection.
     *
     * @param pipeline The list of Bson stages representing the aggregation pipeline that will be printed.
     */
    private fun printOutPipeline(pipeline: List<Bson>) {
        println("-".repeat(40))
        println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate pipeline:")
        println("*".repeat(40))
        println(pipeline.json2)
        println("+".repeat(40))
    }

    /**
     * Executes the creation query and returns the resulting item state.
     *
     * @param apiItem The query object containing the necessary data for creation.
     * @return An instance of [ItemState] representing the result of the creation operation.
     */
    protected open suspend fun queryCreate(
        apiItem: ApiItem.Upsert.Create.Query<T, ID, FILT>,
    ): ItemState<T> = ItemState(isOk = true)

    /**
     * Executes a read query for the given API item and returns the resulting item state.
     *
     * @param apiItem The API item used to perform the read query.
     * @param item The item to be processed with the read query.
     * @return The resulting item state containing the processed item.
     */
    protected open suspend fun queryRead(
        apiItem: ApiItem.Read<T, ID, FILT>,
        item: T,
    ): ItemState<T> = ItemState(item = item)

    /**
     * Executes a query-based update operation on the given item.
     *
     * @param apiItem An instance of ApiItem.Upsert.Update.Query containing information about the update query.
     * @param item The item to be updated.
     * @return The updated state of the item wrapped in an ItemState.
     */
    protected open suspend fun queryUpdate(
        apiItem: ApiItem.Upsert.Update.Query<T, ID, FILT>,
        item: T,
    ): ItemState<T> = ItemState(item = item)

    /**
     * Handles the process of querying and deleting an item within the specified context.
     *
     * @param apiItem The API query item configuration containing information for the delete operation.
     * @param item The item to be deleted.
     * @return The resulting state of the item after the delete operation.
     */
    protected open suspend fun queryDelete(
        apiItem: ApiItem.Delete.Query<T, ID, FILT>,
        item: T,
    ): ItemState<T> = ItemState(item = item)

    /**
     * Refactors the given pipeline right after the [buildLookupList] fun by applying a result unit and an API filter.
     *
     * @param pipeline a mutable list of Bson elements representing the data pipeline
     * @param resultUnit an instance of the ResultUnit to apply to the pipeline
     * @param apiFilter an instance of FILT filter to apply to the pipeline, with a default of commonContainer.apiFilterInstance()
     * @return the refactored pipeline as a mutable list of Bson elements
     */
    open fun refactorPipeline(
        pipeline: MutableList<Bson> = mutableListOf(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        apiRequestParams: ApiRequestParams? = null,
        resultUnit: ResultUnit,
    ) {
    }

    /**
     * Generates a MongoDB BSON sort stage based on the provided filter.
     *
     * @param apiFilter the filter object specifying the sorting configuration.
     * @return a BSON object representing the sort stage, or null if no sort stage is defined.
     */
    open fun sortStage(apiFilter: FILT): Bson? = null

    /**
     * Updates specific fields of an item identified by its ID.
     * Validates nullability constraints of fields being updated and performs pre-update and post-update actions,
     * including permission checks and custom callbacks.
     *
     * @param call the [ApplicationCall] object, which can be null, used for context-specific operations such as permission verification.
     * @param id the unique identifier of the item to be updated.
     * @param filter an optional BSON filter to further qualify the update operation.
     * @param fieldAssignments a list of field assignments specifying the fields to update and their new values.
     * @return an [ItemState] object indicating the result of the update operation, including success state and any error messages.
     */
    @OptIn(InternalSerializationApi::class)
    @Suppress("unused")
    suspend fun updateFieldsById(
        call: ApplicationCall? = null,
        id: ID,
        vararg fieldAssignments: AssignTo<T, *>,
        filter: Bson? = null,
    ): ItemState<T> {
        if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
        fieldAssignments.forEach { it ->
            if (it.kField.returnType.isMarkedNullable.not() && it.value == null) {
                return ItemState(
                    isOk = false,
                    msgError = "Field '${it.kField.name}' is marked as non-nullable, but null value provided."
                )
            }
        }
        call?.let {
            val s: SimpleState = getCrudPermission(call = it, crudTask = CrudTask.Update)
            if (s.hasError) return ItemState(isOk = false, msgError = s.msgError)
        }
        val item = coroutine.findById(id = id) ?: return ItemState(isOk = false, msgError = "Item not found")
        var apiItem = ApiItem.Upsert.Update.Action(
            item = item.copyItemWithPrimaryConstructorParameters(
                *fieldAssignments
            ),
            apiFilter = commonContainer.apiFilterInstance(),
            orig = item,
            call = call,
        )
        onPermissionUpsert(apiItem).also { if (it.hasError) return it.asItemState() }
        onPermissionUpsertUpdate(apiItem, apiItem.item).also { if (it.hasError) return it.asItemState() }
        onBeforeUpsertAction(apiItem = apiItem).also { it ->
            if (it.hasError) return it
            it.item?.let {
                apiItem = apiItem.copy(item = it.copyItemWithPrimaryConstructorParameters())
            }
        }
        onBeforeUpsertUpdateAction(apiItem = apiItem).also { it ->
            if (it.hasError) return it
            it.item?.let {
                apiItem = apiItem.copy(item = it.copyItemWithPrimaryConstructorParameters())
            }
        }
        val result: UpdateResult = try {
            apiItem = apiItem.copy(item = apiItem.item.copyItemWithPrimaryConstructorParameters())
            if (apiItem.item.json == apiItem.orig?.json) return ItemState(
                isOk = false,
                msgError = "Update skipped - no changes detected in item"
            )
            onValidate(apiItem, apiItem.item).also { if (it.hasError) return it.asItemState() }
            coroutine.updateOne(
                filter = and(BaseDoc<*>::_id eq id, filter ?: EMPTY_BSON),
                target = apiItem.item,
            )
        } catch (e: Exception) {
            onAfterUpsertUpdateAction(apiItem = apiItem, result = false)
            onAfterUpsertAction(apiItem = apiItem, result = false)
            return ItemState(isOk = false, msgError = e.message)
        }
        val itemState = when (result.modifiedCount) {
            1L -> findItemStateById(id = id)
            0L -> ItemState(state = State.Warn, msgError = "Field not modified")
            else -> ItemState(isOk = false)
        }
        onAfterUpsertUpdateAction(apiItem = apiItem, result = itemState.hasError.not())
        onAfterUpsertAction(apiItem = apiItem, result = itemState.hasError.not())
        return itemState
    }

    /**
     * Updates a single item in the database.
     *
     * @param item The item to be updated.
     * @param orig The original item before update, if available. Defaults to null.
     * @param filter The filter to identify the item to be updated. Defaults to null.
     * @param apiFilter The API filter instance for the update operation. Defaults to a common API filter instance.
     * @param updateOptions Options to apply during the update operation. Defaults to an instance of UpdateOptions.
     * @return The state of the item after the update operation.
     */
    @Suppress("unused")
    suspend fun updateOne(
        item: T,
        orig: T? = null,
        filter: Bson? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        updateOptions: UpdateOptions = UpdateOptions(),
        call: ApplicationCall? = null,
    ): ItemState<T> {
        return updateOne(
            apiItem = ApiItem.Upsert.Update.Action(
                item = item.copyItemWithPrimaryConstructorParameters(),
                apiFilter = apiFilter,
                orig = orig,
                call = call,
            ),
            filter = filter,
            updateOptions = updateOptions
        )
    }

    /**
     * Updates a single item in the database based on the provided filter.
     *
     * @param apiItem The API item containing the item id [ApiItem] to update and other relevant information.
     * @param filter The filter to apply when updating the item. If null, no filter will be applied.
     * @param updateOptions The options to use when updating the item.
     *
     * @return The state of the item after the update operation.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun updateOne(
        apiItem: ApiItem.Upsert.Update.Action<T, ID, FILT>,
        filter: Bson? = null,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemState<T> {
        if (readOnly) return ItemState(isOk = false, msgError = readOnlyErrorMsg)
        onPermissionUpsert(apiItem).also { if (it.hasError) return it.asItemState() }
        onPermissionUpsertUpdate(
            apiItem = apiItem,
            item = apiItem.item
        ).also { if (it.hasError) return it.asItemState() }
        var apiItem1 = apiItem.copy(item = apiItem.item.copyItemWithPrimaryConstructorParameters())
        onBeforeUpsertAction(apiItem = apiItem1).also { it ->
            if (it.hasError) return it
            it.item?.let {
                apiItem1 = apiItem1.copy(item = it.copyItemWithPrimaryConstructorParameters())
            }
        }
        onBeforeUpsertUpdateAction(apiItem = apiItem1).also { it ->
            if (it.hasError) return it
            it.item?.let {
                apiItem1 = apiItem1.copy(item = it.copyItemWithPrimaryConstructorParameters())
            }
        }
        val filter1 = and(BaseDoc<ID>::_id eq apiItem1.item._id, filter ?: EMPTY_BSON)
        val updateResult = try {
            apiItem1 = apiItem1.copy(item = apiItem1.item.copyItemWithPrimaryConstructorParameters())
            if (apiItem1.item.json == apiItem1.orig?.json) return ItemState(
                isOk = false,
                msgError = "Update skipped - no changes detected in item"
            )
            onValidate(apiItem1, apiItem1.item).also { if (it.hasError) return it.asItemState() }
            mongoColl.coroutine.updateOne(
                filter = filter1,
                target = apiItem1.item,
                options = updateOptions
            )
        } catch (e: Exception) {
            onAfterUpsertUpdateAction(apiItem = apiItem1, result = false)
            onAfterUpsertAction(apiItem = apiItem1, result = false)
            return ItemState(isOk = false, msgError = e.message)
        }
        val state: State
        val noDataModified: Boolean
        if (updateResult.matchedCount > 0) {
            if (updateResult.modifiedCount == 1L) {
                state = State.Ok
                noDataModified = false
            } else {
                state = State.Warn
                noDataModified = true
            }
        } else {
            if (updateOptions.isUpsert && updateResult.upsertedId != null) {
                state = State.Ok
                noDataModified = false
            } else {
                state = State.Error
                noDataModified = true
            }
        }
        onAfterUpsertUpdateAction(apiItem = apiItem1, result = state != State.Error)
        onAfterUpsertAction(apiItem = apiItem1, result = state != State.Error)
        return if (state != State.Error) {
            ItemState(
                item = apiItem1.item,
                state = state,
                noDataModified = noDataModified,
                msgError = "No data was modified ..."
            )
        } else {
            ItemState(
                isOk = false,
                msgError = "${commonContainer.labelItemId(apiItem1.item)} not found with [ ${filter1.json} ]"
            )
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            with(coroutine) {
                onAfterOpen()
                indexes()
            }
        }
    }

    @Serializable
    enum class CountType {
        PreLookup,
        PostLookup,
        Estimated,
        Unknown
    }

    data class PageCountInfo(
        val match: Bson? = null,
        val pipeline: List<Bson>? = null,
        var lastPage: Int? = null,
        var lastRow: Int? = null,
        val countType: CountType,
    ) {
        suspend fun count(coll: Coll<*, *, *, *>, pageSize: Int) {
            val count = when (countType) {
                CountType.PreLookup ->
                    coll.mongoColl.coroutine.countDocuments(
                        match?.let {
                            if (Document.parse(match.json).isNotEmpty())
                                it
                            else
                                EMPTY_BSON
                        } ?: EMPTY_BSON
                    )

                CountType.PostLookup -> pipeline?.let {
                    coll.mongoColl.coroutine.aggregate<Document>(it).first()?.getInteger("count")
                        ?.toLong()
                }

                CountType.Estimated -> coll.mongoColl.coroutine.estimatedDocumentCount()
                CountType.Unknown -> null
            }
            count?.let {
                if (pageSize > 0) {
                    lastPage = (count / pageSize + if (count.toInt() % pageSize > 0) 1 else 0).toInt()
                }
                lastRow = it.toInt()
            }
        }
    }

    enum class ResultUnit {
        Single,
        List,
    }

    init {
        if (this is IRoleInUserColl<*, *, *, *, *, *>) {
            privateRoleInUserColl = this
        }
    }
}
