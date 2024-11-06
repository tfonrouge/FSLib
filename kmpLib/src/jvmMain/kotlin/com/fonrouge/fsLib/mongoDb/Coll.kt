package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.FieldPath
import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.*
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.model.state.State
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.LongId
import com.fonrouge.fsLib.serializers.StringId
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.property.KPropertyPath
import java.util.*
import kotlin.jvm.internal.PropertyReference1Impl
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses

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
        var name: String? = null
        do {
            name = self?.findAnnotation<Collection>()?.name
            if (name == null) {
                self = self?.superclasses?.firstOrNull()
            }
        } while (name == null && self != Any::class)
        return name ?: simpleName ?: ""
    }

/**
 * Represents a MongoDb collection
 *
 * @property commonContainer The common container instance.
 * @property debug The debug flag indicating if debugging is enabled.
 * @property children Set of children associated with the collection.
 * @property readOnly Indicates if the collection is read-only.
 * @property lookupFun Function for performing lookups.
 * @property mongoColl The MongoDB collection instance.
 * @property coroutine The CoroutineScope used for handling suspending functions.
 */
abstract class Coll<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val commonContainer: CC,
    private var debug: Boolean? = null
) {
    companion object {
        var globalDebug = false
    }

    /**
     * A lambda function that, when invoked, returns a list of KProperty1 instances representing
     * properties of type ID? within a subclass of BaseDoc. These properties are typically
     * used to define relationships or associations within a document.
     */
    open val children: (() -> List<KProperty1<out BaseDoc<*>, ID?>>)? = null

    /**
     * Filters items based on the criteria specified in the given filter.
     *
     * @param apiFilter the filter criteria to apply when searching for items.
     * @return a BSON object representing the filter to be applied, or null if no filter is specified.
     */
    open fun findItemFilter(apiFilter: FILT): Bson? = null

    /**
     * Indicates if the current instance should be read-only.
     *
     * This variable determines whether the instance can be modified or not.
     * If set to `true`, the instance is immutable and cannot be changed.
     * If set to `false`, the instance is mutable and can be altered.
     */
    open val readOnly = false

    /**
     * Processes an API request and returns an item state.
     *
     * @param iApiItem The API item to be processed.
     * @param user The user making the request, if available.
     * @param call The application call instance, if available.
     * @param userRoleColl The user role collection, if available.
     * @param kCallable The KCallable instance related to the action, if available.
     * @param stackTraceElement The stack trace element from the caller.
     * @return Returns the state of the item based on the processed API request.
     */
    @Suppress("unused")
    suspend fun apiItemProcess(
        iApiItem: IApiItem<T, ID, FILT>,
        user: IUser<*>? = null,
        call: ApplicationCall? = null,
        userRoleColl: IUserRoleColl<*, *, *, *, *, *>? = null,
        kCallable: KCallable<*>? = null,
        stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[2]
    ): ItemState<T> {
        val user1 = user ?: userRoleColl?.let { call?.sessions?.get(userRoleColl.userKClass) }
        userRoleColl?.getUserPermission(
            user = user1,
            kCallable = kCallable,
            stackTraceElement = stackTraceElement
        )?.let {
            if (it.state == State.Error) return ItemState(it)
        }
        return when (val apiItem = iApiItem.asApiItem(commonContainer)) {
            is ApiItem.Upsert -> {
                if (readOnly) return ItemState(isOk = false, msgError = "Collection is read-only")
                onBeforeUpsert(apiItem).also { if (it.hasError) return it }
                when (apiItem) {
                    is ApiItem.Upsert.Create -> when (apiItem) {
                        is ApiItem.Upsert.Create.Query -> {
                            onBeforeUpsertCreate(apiItem = apiItem).also { if (it.hasError) return it }
                            queryCreate(
                                apiItem = apiItem,
                                iUser = user1
                            )
                        }

                        is ApiItem.Upsert.Create.Action -> actionCreate(
                            apiItem = apiItem,
                            iUser = user1
                        )
                    }

                    is ApiItem.Upsert.Update -> when (apiItem) {
                        is ApiItem.Upsert.Update.Query -> {
                            val itemState = findItemState(apiItem)
                            val item = itemState.item
                            if (itemState.hasError || item == null) return itemState
                            onBeforeUpsertUpdate(apiItem = apiItem, item = item).also { if (it.hasError) return it }
                            queryUpdate(
                                apiItem = apiItem,
                                itemState = itemState,
                                iUser = user1
                            )
                        }

                        is ApiItem.Upsert.Update.Action -> actionUpdate(
                            apiItem = apiItem,
                            iUser = user1
                        )
                    }
                }
            }

            is ApiItem.Read -> {
                val itemState = onBeforeRead(apiItem = apiItem).also { if (it.hasError) return it }
                val item = itemState.item
                if (itemState.hasError || item == null) return itemState
                queryRead(
                    apiItem = apiItem,
                    itemState = itemState,
                    iUser = user1
                )
            }

            is ApiItem.Delete -> {
                if (readOnly) return ItemState(isOk = false, msgError = "Collection is read-only")
                when (apiItem) {
                    is ApiItem.Delete.Query -> {
                        val itemState = findChildrenNot(apiItem.id)
                        val item = itemState.item
                        if (itemState.hasError || item == null) return itemState
                        onBeforeDelete(apiItem = apiItem, item = item).also { if (it.hasError) return it }
                        queryDelete(apiItem = apiItem, itemState = itemState, iUser = user1)
                    }

                    is ApiItem.Delete.Action -> actionDelete(apiItem = apiItem, iUser = user1)
                }
            }
        }
    }

    /**
     * Executes a query to create an item represented by the provided API item.
     *
     * @param apiItem The API item containing the creation details, of type ApiItem.Query.Upsert.Create.
     * @param iUser Optional parameter representing the user performing the operation, of type IUser.
     * @return The state of the item after the creation operation, encapsulated in an ItemState object.
     */
    protected open suspend fun queryCreate(
        apiItem: ApiItem.Upsert.Create.Query<T, ID, FILT>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = ItemState(isOk = true)

    /**
     * Executes a read query for the specified API item.
     *
     * @param apiItem The API item containing read query details.
     * @param itemState The current state of the item being queried.
     * @param iUser The user performing the query, optional.
     * @return The updated state of the item after the query is executed.
     */
    protected open suspend fun queryRead(
        apiItem: ApiItem.Read<T, ID, FILT>,
        itemState: ItemState<T>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = itemState

    /**
     * Handles the update operation for the provided API query item.
     *
     * @param apiItem The API item representing the update query.
     * @param itemState The current state of the item to be updated.
     * @param iUser The user performing the update operation, can be null.
     * @return The updated state of the item.
     */
    protected open suspend fun queryUpdate(
        apiItem: ApiItem.Upsert.Update.Query<T, ID, FILT>,
        itemState: ItemState<T>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = itemState

    /**
     * Executes a delete query on the specified item and returns the resulting state.
     *
     * @param apiItem The API item representing the delete query.
     * @param itemState The current state of the item to be deleted.
     * @param iUser The user performing the delete operation, optional.
     * @return The new state of the item after the delete operation.
     */
    protected open suspend fun queryDelete(
        apiItem: ApiItem.Delete.Query<T, ID, FILT>,
        itemState: ItemState<T>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = itemState

    /**
     * Handles the creation action for an API item.
     *
     * @param apiItem the item to be created, encapsulated in an ApiItem.Action.Upsert.Create object.
     * @param iUser optional user information associated with the action.
     * @return the state of the item after the creation process.
     */
    protected open suspend fun actionCreate(
        apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = insertOne(apiItem)

    /**
     * Performs the update action on the given API item.
     *
     * @param apiItem The API item encapsulating the details required for the update.
     * @param iUser The user initiating the action, can be null.
     * @return The state of the item after the update has been performed.
     */
    protected open suspend fun actionUpdate(
        apiItem: ApiItem.Upsert.Update.Action<T, ID, FILT>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = updateOne(apiItem)

    /**
     * Handles the deletion of an item based on the provided ApiItem.Action.Delete instance.
     *
     * @param apiItem The ApiItem.Action.Delete instance containing details for the deletion.
     * @param iUser The user performing the deletion, nullable.
     * @return The state of the item after the deletion.
     */
    protected open suspend fun actionDelete(
        apiItem: ApiItem.Delete.Action<T, ID, FILT>,
        iUser: IUser<*>? = null,
    ): ItemState<T> = deleteOne(apiItem)

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
     * A function that takes a filter of type `FILT` and returns a list of `LookupPipelineBuilder` instances.
     *
     * This function is designed to perform lookups based on the provided filter and build a pipeline of
     * lookup operations. By default, it returns an empty list, indicating no lookups.
     *
     * @param FILT the type of the filter used for lookups.
     * @return a list of `LookupPipelineBuilder` instances based on the provided filter.
     */
    open val lookupFun: (FILT) -> List<LookupPipelineBuilder<T, *, *>> = { listOf() }

    val mongoColl: MongoCollection<T> =
        mongoDatabase.getCollection(
            commonContainer.itemKClass.collectionName,
            commonContainer.itemKClass.java
        )

    /**
     * A coroutine-based collection instance derived from a MongoDB collection.
     * This variable provides coroutine support for asynchronous operations
     * on the MongoDB collection, enabling more efficient and non-blocking database
     * interactions.
     *
     * @param T The type of documents stored within the MongoDB collection.
     */
    val coroutine: CoroutineCollection<T> = mongoColl.coroutine

    /**
     * Aggregates a lookup publisher with a provided pipeline and lookups.
     *
     * @param pipeline The mutable list of BSON stages to be applied to the aggregation pipeline.
     * @param lookups The list of lookup wrapper objects to be used for aggregation.
     * @param apiFilter An instance of a common filter type used for API filtering.
     * @param listFirstStage The first stage of the list, which may include pre- and post-lookup match and sort stages.
     * @param countType The type of counting to be done, pre-lookup, post-lookup, estimated, or unknown.
     * @param debug A flag to indicate whether debugging information should be printed.
     * @param pageStateInfoFun A function to handle page count information, called with a PageCountInfo object.
     * @param postProcessPipeline A function to post-process the pipeline.
     * @return An AggregatePublisher that executes the aggregation pipeline with the applied stages and lookups.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun aggregateLookupPublisher(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookups: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        listFirstStage: ListFirstStage? = null,
        countType: CountType = CountType.PreLookup,
        debug: Boolean? = this.debug,
        pageStateInfoFun: ((PageCountInfo) -> Unit)? = null,
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
    ): AggregatePublisher<T> {
        listFirstStage?.preLookupMatch?.let {
            if (Document.parse(it.json).isNotEmpty()) pipeline.add(
                match(it)
            )
        }
        listFirstStage?.preLookupSort?.let { pipeline.add(sort(it)) }
        buildPipeline(
            pipeline = pipeline,
            lookups = lookups,
            resultUnit = ResultUnit.List,
            apiFilter = apiFilter
        )
        postProcessPipeline?.let { it(pipeline) }
        listFirstStage?.postLookupMatch?.let {
            if (Document.parse(it.json).isNotEmpty()) pipeline.add(
                match(it)
            )
        }
        listFirstStage?.postLookupSort?.let { pipeline.add(sort(it)) }
        listFirstStage?.let {
            val pageCountInfo: PageCountInfo = when (countType) {
                CountType.PreLookup -> PageCountInfo(
                    match = listFirstStage.preLookupMatch,
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
            it.page?.let { page ->
                it.pageSize?.let { pageSize ->
                    (pageSize * (page - 1)).let { skip -> if (skip > 0) pipeline.add(skip(skip)) }
                    pipeline.add(limit(pageSize))
                }
            }
        }
        if (debug ?: globalDebug) {
            println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate pipeline:")
            println(pipeline.json)
        }
        return mongoColl.aggregate(pipeline, commonContainer.itemKClass.java)
    }

    /**
     * Aggregates data with a specified lookup in a MongoDB collection.
     *
     * @param pipeline an initial list of BSON operations to start the aggregation pipeline.
     * @param lookups a list of lookup wrappers to specify join conditions in the aggregation.
     * @param apiFilter a filter applied to the API, defaulting to a common container instance.
     * @param postProcessPipeline an optional lambda to further process the pipeline after it is built.
     * @return an AggregatePublisher that provides asynchronous access to the aggregated data.
     */
    @Suppress("unused")
    suspend fun aggregateOneLookup(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookups: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
    ): AggregatePublisher<T> {
        buildPipeline(
            pipeline = pipeline,
            lookups = lookups,
            resultUnit = ResultUnit.One,
            apiFilter = apiFilter
        )
        postProcessPipeline?.let { it(pipeline) }
        if (debug ?: globalDebug) {
            println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate pipeline:")
            println(pipeline.json)
        }
        return mongoColl.aggregate(pipeline, commonContainer.itemKClass.java)
    }

    /**
     * Retrieves a list of container items based on the provided parameters.
     *
     * @param listFirstStage The first stage of the pipeline for aggregating the items.
     * @param lookupWrappers The list of lookup wrappers to perform lookups on the items.
     * @param postProcessPipeline The pipeline to post-process the MongoDB aggregation pipeline.
     * @param apiFilter The API filter for filtering the items.
     * @param countType The type of count to perform on the items.
     * @param debug Indicates whether debugging should be enabled.
     * @param postProcessList The function to post-process the retrieved list of items.
     * @return The resulting list state containing the serialized items and pagination information.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun apiListProcess(
        listFirstStage: ListFirstStage,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        countType: CountType = CountType.PreLookup,
        debug: Boolean? = this.debug,
        postProcessList: ((List<T>) -> List<T>)? = null,
    ): ListState<T> {
        var pageCountInfo: PageCountInfo? = null
        val publisher = aggregateLookupPublisher(
            pipeline = listFirstStage.pipeline,
            lookups = lookupWrappers,
            apiFilter = apiFilter,
            listFirstStage = listFirstStage,
            countType = countType,
            debug = debug,
            postProcessPipeline = postProcessPipeline,
            pageStateInfoFun = {
                pageCountInfo = it
            }
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
                listFirstStage.pageSize?.let {
                    pageCountInfo?.count(this@Coll, listFirstStage.pageSize)
                }
                t2 = Date().time - curTime
            }
            joinAll(j1, j2)
        }
        if (debug ?: globalDebug) {
            println("Class: ${commonContainer.itemKClass.simpleName} ('${commonContainer.itemKClass.collectionName}'), Aggregate time: ${t1}ms, Count time: ${t2}ms")
        }
        val data = Json.encodeToString(
            serializer = ListSerializer(elementSerializer = commonContainer.itemSerializer),
            value = postProcessList?.let { it(list) } ?: list
        )
        return ListState(
            data = data,
            last_page = pageCountInfo?.lastPage,
            last_row = pageCountInfo?.lastRow,
        )
    }

    /**
     * Processes the given `ApiList` through a multistep pipeline which includes matching, sorting, and
     * various transformations specified by the provided arguments. The main processing steps are
     * executed before and after lookups are applied, with additional options for post-processing
     * both the pipeline and the resulting list.
     *
     * @param preLookupMatch the BSON filter to apply before performing lookups
     * @param postLookupMatch the BSON filter to apply after performing lookups
     * @param preLookupSort the BSON sort expression to apply before performing lookups
     * @param postLookupSort the BSON sort expression to apply after performing lookups
     * @param apiList the `ApiList` object containing pagination, filter, and sorting information
     * @param countType the type of count operation to perform (before or after lookups)
     * @param debug an optional flag to enable or disable debug mode
     * @param lookupWrappers a list of `LookupWrapper` objects specifying the lookup stages to apply
     * @param postProcessPipeline an optional lambda function to apply additional transformations to the pipeline
     * @param postProcessList an optional lambda function to apply transformations to the final result list
     * @return the processed `ListState` containing the final list and metadata such as count and pagination
     */
    @Suppress("unused")
    suspend fun apiListProcess(
        preLookupMatch: Bson? = null,
        postLookupMatch: Bson? = null,
        preLookupSort: Bson? = null,
        postLookupSort: Bson? = null,
        apiList: ApiList<FILT>,
        countType: CountType = CountType.PreLookup,
        debug: Boolean? = this.debug,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        postProcessList: ((List<T>) -> List<T>)? = null
    ): ListState<T> {
        return apiListProcess(
            listFirstStage = listFirstStage(
                preLookupMatch = preLookupMatch,
                postLookupMatch = postLookupMatch,
                preLookupSort = preLookupSort,
                postLookupSort = postLookupSort,
                page = apiList.tabPage,
                size = apiList.tabSize,
                filter = apiList.tabFilter,
                sorter = apiList.tabSorter,
            ),
            lookupWrappers = lookupWrappers,
            postProcessPipeline = postProcessPipeline,
            apiFilter = apiList.apiFilter,
            countType = countType,
            debug = debug,
            postProcessList = postProcessList
        )
    }

    /**
     * Builds a pipeline of BSON lookup stages for aggregation queries.
     *
     * @param lookupWrappers A list of lookup wrappers that define custom lookup operations. Defaults to an empty list.
     * @param apiFilter A filter instance used to customize the lookup behavior.
     * @return A mutable list of BSON stages for the lookup pipeline.
     */
    private suspend fun buildLookupList(
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
    ): MutableList<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        val lookupPipelineBuilders =
            lookupFun(apiFilter).plus(lookupWrappers.mapNotNull {
                if (it is LookupByPipeline<*, *, *>) it.pipeline else null
            })
        lookupPipelineBuilders.forEach { lookupPipelineBuilder ->
            val lookupWrapper = lookupWrappers.find {
                lookupPipelineBuilder.resultProperty == when (it) {
                    is LookupByProperty -> it.resultProperty
                    is LookupByPipeline<*, *, *> -> it.pipeline.resultProperty
                    else -> null
                }
            }
            if (lookupWrapper != null) {
                pipeline += lookupPipelineBuilder.pipelineList(lookupWrapper)
            } else {
                fixedLookupList(apiFilter)?.find { kProperty1 -> kProperty1 == lookupPipelineBuilder.resultProperty }
                    ?.let {
                        pipeline += lookupPipelineBuilder.pipelineList()
                    }
            }
        }
        return pipeline
    }

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

    private fun checkDontPersist(item: T) {
        item::class.memberProperties.forEach { kProperty1 ->
            if (kProperty1.hasAnnotation<DontPersist>() && kProperty1 is KMutableProperty1) {
                kProperty1.setter.call(item, null)
            }
        }
    }

    /**
     * Finds the children of an item specified by the given ID that do not match certain conditions.
     *
     * @param id The ID of the item to find children for.
     * @return An ItemState indicating the result of the find operation,
     *         including potential errors or states.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findChildrenNot(
        id: ID,
    ): ItemState<T> {
        val itemState = findItemStateById(id)
        if (itemState.hasError.not()) {
            children?.invoke()?.forEach { kProperty1: KProperty1<out BaseDoc<*>, ID?> ->
                when (kProperty1) {
                    is FieldPath -> kProperty1.path to kProperty1.owner.collectionName
                    is PropertyReference1Impl -> (kProperty1.owner as KClass<*>)
                        .findAnnotation<Collection>()?.name?.let { kProperty1.name to it }

                    is KPropertyPath<*, ID?> -> return ItemState(
                        state = State.Error,
                        msgError = "Child field path '${commonContainer.itemKClass.simpleName}::${kProperty1.path()}' not valid (if you used '/' or '%' operators you must use '+' operator)"
                    )

                    else -> null
                }?.let { (fieldName: String, collectionName) ->
                    mongoDatabase.getCollection(collectionName).also { mongoCollection ->
                        mongoCollection.coroutine.find(Document(fieldName, id)).first()?.let {
                            return ItemState(
                                state = State.Error,
                                msgError = "'${commonContainer.labelItem}' has children in '$collectionName.$fieldName'"
                            )
                        }
                    }
                }
            }
        }
        return itemState
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
        if (readOnly) return ItemState(isOk = false, msgError = "Collection is read-only")
        return try {
            onBeforeDelete(apiItem = apiItem, item = apiItem.item).also {
                if (it.hasError) {
                    onAfterDeleteAction(apiItem = apiItem, result = false)
                    return it
                }
            }
            val result = coroutine.deleteOne(
                and(
                    BaseDoc<*>::_id eq apiItem.item._id,
                    filter ?: EMPTY_BSON
                )
            ).deletedCount == 1L
            onAfterDeleteAction(apiItem = apiItem, result = result)
            ItemState(
                isOk = result,
                msgOk = "Delete operation ok"
            )
        } catch (e: Exception) {
            onAfterDeleteAction(apiItem = apiItem, result = false)
            ItemState(isOk = false, msgError = e.message)
        }
    }

    /**
     * Ensures that the indexes for the collection are created and exist.
     *
     * This suspend function goes through the necessary steps to ensure that all required indexes on the collection are properly set up.
     * It is intended to be used within a coroutine context to leverage Kotlin's asynchronous programming features.
     *
     * @receiver CoroutineCollection<T> The collection for which the indexes need to be ensured.
     */
    open suspend fun CoroutineCollection<T>.ensureIndexes() {
    }

    /**
     * Constructs and modifies the provided aggregation pipeline.
     *
     * @param pipeline The initial mutable list of Bson objects representing the pipeline stages.
     * @param lookups A list of LookupWrapper instances to build lookup stages.
     * @param resultUnit The result unit used for refactoring the pipeline.
     * @param apiFilter An instance of the FILT configuration for filtering the pipeline.
     * @return The modified list of Bson objects representing the complete pipeline.
     */
    suspend fun buildPipeline(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookups: List<LookupWrapper<*, *>> = emptyList(),
        resultUnit: ResultUnit,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
    ): MutableList<Bson> {
        pipeline.addAll(
            refactorPipeline(
                pipeline = buildLookupList(lookupWrappers = lookups, apiFilter = apiFilter),
                resultUnit = resultUnit,
                apiFilter = apiFilter
            )
        )
        return pipeline
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
    suspend fun findPublisher(
        filter: Bson? = null,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        debug: Boolean = false,
    ): AggregatePublisher<T> {
        return aggregateLookupPublisher(
            pipeline = filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(),
            lookups = lookupWrappers,
            apiFilter = apiFilter,
            debug = debug,
        )
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
        debug: Boolean = false,
    ): List<T> {
        return findPublisher(
            filter = filter,
            lookupWrappers = lookupWrappers,
            apiFilter = apiFilter,
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
            debug = debug,
        ).awaitFirstOrNull()
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

    @Suppress("unused")
    suspend fun insertOne(
        item: T,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        overrideValidation: Boolean = false
    ): ItemState<T> = insertOne(
        apiItem = ApiItem.Upsert.Create.Action(
            item = item,
            apiFilter = apiFilter
        ),
        overrideValidation = overrideValidation
    )

    /**
     * Inserts a single item into the database on a Query state
     *
     * @param apiItem the API item representing the request to upsert the item
     * @param item the item to be inserted
     * @param overrideValidation flag indicating whether to override validation rules
     * @return the [ItemState] representing the state of the operation
     */
    @Suppress("unused")
    suspend fun insertOne(
        apiItem: ApiItem.Upsert.Create.Query<T, ID, FILT>,
        item: T,
        overrideValidation: Boolean = false
    ): ItemState<T> {
        val itemState = insertOne(
            apiItem = ApiItem.Upsert.Create.Action(item, apiItem.apiFilter),
            overrideValidation = overrideValidation
        )
        return itemState.copy(itemAlreadyOn = true)
    }

    /**
     * Inserts a single item into the database.
     *
     * @param apiItem The API item containing the item to be inserted.
     * @param overrideValidation Flag indicating whether to override the item validation. (Default: false)
     * @return The state of the item after insertion.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun insertOne(
        apiItem: ApiItem.Upsert.Create.Action<T, ID, FILT>,
        overrideValidation: Boolean = false
    ): ItemState<T> {
        if (readOnly) return ItemState(isOk = false, msgError = "Collection is read-only")
        val item = apiItem.item
        if (!overrideValidation) {
            commonContainer.validateItem(item = item, apiItem.apiFilter).also { itemState ->
                if (itemState.hasError) return itemState
            }
        }
        checkDontPersist(item)
        return try {
            onBeforeUpsertCreate(apiItem).also {
                if (it.hasError) {
                    onAfterUpsertAction(apiItem = apiItem, result = false)
                    return it
                }
            }
            val insertOneResult: InsertOneResult = mongoColl.insertOne(item).awaitSingle()
            val result = insertOneResult.insertedId != null
            onAfterUpsertAction(apiItem = apiItem, result = result)
            ItemState(
                item = item,
                state = if (result) State.Ok else State.Error,
            )
        } catch (e: Exception) {
            onAfterUpsertAction(apiItem = apiItem, result = false)
            ItemState(isOk = false, msgError = e.message)
        }
    }

    private fun findFieldType(kClass: KClass<*>, fieldName: String): KClassifier? {
        val k = kClass.memberProperties
        return if (fieldName.contains('.')) {
            k.firstOrNull { it.name == fieldName.substringBefore('.') }?.returnType?.classifier?.let {
                findFieldType(it as KClass<*>, fieldName.substringAfter('.'))
            }
        } else {
            k.firstOrNull { it.name == fieldName }?.returnType?.classifier
        }
    }

    private fun listFirstStage(
        preLookupMatch: Bson? = null,
        postLookupMatch: Bson? = null,
        preLookupSort: Bson? = null,
        postLookupSort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
    ): ListFirstStage {
        val pipeline = mutableListOf<Bson>()
        val postLookupMatchList: MutableList<Bson> = mutableListOf()
        postLookupMatch?.let { postLookupMatchList.add(it) }
        filter?.let {
            val result = mutableListOf<Bson>()
            filter.forEach { remoteFilter ->
                val value: BsonValue? =
                    when (findFieldType(commonContainer.itemKClass, remoteFilter.field)) {
                        Array<String>::class, String::class, StringId::class, null -> {
                            when (remoteFilter.type) {
                                "like" -> BsonDocument(
                                    "\$regex",
                                    BsonString(remoteFilter.value)
                                ).append("\$options", BsonString("i"))

                                else -> BsonString(remoteFilter.value)
                            }
                        }

                        Int::class, IntId::class -> remoteFilter.value?.toIntOrNull()
                            ?.let { BsonInt32(it) }

                        Long::class, LongId::class -> remoteFilter.value?.toLongOrNull()
                            ?.let { BsonInt64(it) }

                        Double::class -> remoteFilter.value?.toDoubleOrNull()
                            ?.let { BsonDouble(it) }

                        else -> null
                    }
                value?.let {
                    result.add(BsonDocument(remoteFilter.field, value))
                }
            }
            if (result.isNotEmpty()) postLookupMatchList.add(and(result))
        }
        var sortDocument: Bson? = null
        if (preLookupSort != null) {
            sortDocument = preLookupSort
        } else if (!sorter.isNullOrEmpty()) {
            sortDocument = BsonDocument()
            sorter.forEach { remoteSorter ->
                sortDocument.append(
                    remoteSorter.field, when (remoteSorter.dir) {
                        "asc" -> BsonInt32(1)
                        "desc" -> BsonInt32(-1)
                        else -> BsonInt32(1)
                    }
                )
            }
        }
        return ListFirstStage(
            pipeline = pipeline,
            pageSize = size,
            page = page,
            preLookupMatch = preLookupMatch,
            postLookupMatch = and(postLookupMatchList),
            preLookupSort = sortDocument,
            postLookupSort = postLookupSort
        )
    }

    /**
     * Function to be called immediately after an entity is opened.
     * This is a suspend function, allowing for asynchronous operations.
     *
     * Can be overridden to provide specific behavior upon opening.
     */
    open suspend fun onAfterOpen() = Unit

    /**
     * This method is executed after a delete action is performed.
     *
     * @param apiItem Represents the API item on which the delete action was performed, containing details about the action.
     * @param result The result of the delete action, where true indicates success and false indicates failure.
     */
    open suspend fun onAfterDeleteAction(apiItem: ApiItem.Delete.Action<T, ID, FILT>, result: Boolean) = Unit

    /**
     * This method is executed after the upsert action is performed.
     *
     * @param apiItem The item that was involved in the upsert action containing
     *                Upsert details such as the type T, identifier ID, and filters FILT.
     * @param result  The result of the upsert action as a Boolean value.
     */
    open suspend fun onAfterUpsertAction(apiItem: ApiItem.Upsert<T, ID, FILT>, result: Boolean) = Unit

    /**
     * This method is invoked before deleting an action.
     * It handles pre-deletion logic, such as verifying child items to ensure they can be deleted.
     *
     * @param apiItem The delete action containing the item to be deleted and related information.
     * @return The state of the item before deletion.
     */
    open suspend fun onBeforeDelete(apiItem: ApiItem.Delete<T, ID, FILT>, item: T): ItemState<T> {
        return findChildrenNot(item._id)
    }

    /**
     * This method is executed before reading an item. It retrieves the state of the item
     * using its identifier provided via the apiItem parameter.
     *
     * @param apiItem The API item which contains the identifier required to find the item's state.
     * @return The state of the item identified by the apiItem's id.
     */
    open suspend fun onBeforeRead(apiItem: ApiItem.Read<T, ID, FILT>): ItemState<T> =
        findItemStateById(id = apiItem.id)

    /**
     * Handles actions to be performed before an upsert operation.
     *
     * @param apiItem The API item representing the upsert operation.
     * @return An instance of [ItemState] indicating the state of the item after invoking this action.
     */
    open suspend fun onBeforeUpsert(apiItem: ApiItem.Upsert<T, ID, FILT>): ItemState<T> = ItemState(isOk = true)

    /**
     * This method is called before the upsert create action is performed.
     *
     * @param apiItem An object representing the API item for the upsert create action, containing
     *                the necessary parameters such as type, ID, and filter.
     * @return An ItemState object indicating the status of the action, typically used to signal
     *         whether the operation is considered okay or has issues.
     */
    open suspend fun onBeforeUpsertCreate(apiItem: ApiItem.Upsert.Create<T, ID, FILT>): ItemState<T> =
        ItemState(isOk = true)

    /**
     * Invoked before an upsert update action is performed on the specified API item. This method
     * can be overridden to implement any custom behavior or validation that needs to occur prior
     * to the update action.
     *
     * @param apiItem The API item undergoing the upsert update action.
     * @return An ItemState indicating the status of the pre-update action. By default, it returns a state
     * where `isOk` is true.
     */
    open suspend fun onBeforeUpsertUpdate(apiItem: ApiItem.Upsert.Update<T, ID, FILT>, item: T): ItemState<T> =
        ItemState(isOk = true)

    /**
     * Refactors the given pipeline by applying a result unit and an API filter.
     *
     * @param pipeline a mutable list of Bson elements representing the data pipeline
     * @param resultUnit an instance of the ResultUnit to apply to the pipeline
     * @param apiFilter an instance of FILT filter to apply to the pipeline, with a default of commonContainer.apiFilterInstance()
     * @return the refactored pipeline as a mutable list of Bson elements
     */
    open suspend fun refactorPipeline(
        pipeline: MutableList<Bson> = mutableListOf(),
        resultUnit: ResultUnit,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
    ): MutableList<Bson> = pipeline

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
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemState<T> {
        return updateOne(
            apiItem = ApiItem.Upsert.Update.Action(
                item = item,
                apiFilter = apiFilter,
                orig
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
        if (readOnly) return ItemState(isOk = false, msgError = "Collection is read-only")
        onBeforeUpsertUpdate(apiItem = apiItem, item = apiItem.item).also {
            if (it.hasError) {
                onAfterUpsertAction(apiItem = apiItem, result = false)
                return it
            }
        }
        commonContainer.validateItem(item = apiItem.item, apiFilter = apiItem.apiFilter)
            .also { itemState ->
                if (itemState.hasError) {
                    onAfterUpsertAction(apiItem = apiItem, result = false)
                    return itemState
                }
            }
        checkDontPersist(apiItem.item)
        val filter1 = and(BaseDoc<ID>::_id eq apiItem.item._id, filter ?: EMPTY_BSON)
        val updateResult = try {
            mongoColl.coroutine.updateOne(
                filter = filter1,
                target = apiItem.item,
                options = updateOptions
            )
        } catch (e: java.lang.Exception) {
            onAfterUpsertAction(apiItem = apiItem, result = false)
            e.printStackTrace()
            return ItemState(
                isOk = false,
                msgError = e.message
            )
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
        onAfterUpsertAction(apiItem = apiItem, result = state != State.Error)
        return if (state != State.Error) {
            ItemState(
                item = apiItem.item,
                state = state,
                noDataModified = noDataModified,
                msgError = "No data was modified ..."
            )
        } else {
            ItemState(
                isOk = false,
                msgError = "${commonContainer.labelItemId(apiItem.item)} not found with [ ${filter1.json} ]"
            )
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            with(coroutine) {
                onAfterOpen()
                ensureIndexes()
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
                lastPage = (count / pageSize + if (count.toInt() % pageSize > 0) 1 else 0).toInt()
                lastRow = it.toInt()
            }
        }
    }

    enum class ResultUnit {
        One,
        List,
    }
}
