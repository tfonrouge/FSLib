package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.*
import com.fonrouge.fsLib.model.apiData.*
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.model.state.SimpleState
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
import io.ktor.http.*
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import java.util.*
import kotlin.jvm.internal.PropertyReference1Impl
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

internal val collSet = mutableSetOf<Coll<*, *, *, *>>()

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> buildColl(
    commonContainer: CC,
    debug: Boolean = false
): Coll<CC, T, ID, FILT> = object : Coll<CC, T, ID, FILT>(
    commonContainer = commonContainer,
    debug = debug
) {}

abstract class Coll<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val commonContainer: CC,
    var debug: Boolean? = null
) {
    companion object {
        var globalDebug = false
        fun collectionName(klass: KClass<out BaseDoc<*>>): String =
            klass.findAnnotation<Collection>()?.name ?: klass.simpleName!!
    }

    val collectionName = collectionName(commonContainer.itemKClass)

    /**
     * [List] of [Bson] (lookup result properties) that is *always* added in the [buildLookupList] function
     * for the aggregation operation
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open fun fixedLookupList(
        apiFilter: FILT = commonContainer.apiFilterInstance()
    ): List<KProperty1<in T, *>>? = null

    open val lookupFun: (FILT) -> List<LookupPipelineBuilder<T, *, *>> = { listOf() }

    val mongoColl: MongoCollection<T> =
        mongoDatabase.getCollection(collectionName, commonContainer.itemKClass.java)

    val coroutineColl: CoroutineCollection<T> = mongoColl.coroutine

    /**
     * build an AggregatePublisher<T>.
     *
     * process calls from backend service which provide an [ListFirstStage] from a
     * frontend requiring a list of items.
     * accept a custom pipeline (list of Bson) argument and
     * also accept a list of [LookupWrapper] to be added to the
     * final pipeline on the aggregate operation
     *
     * @param pipeline [Bson] list
     * @param lookups list of lookups to be included [LookupWrapper]
     * @param postProcessPipeline allow to post-process the resulted [Bson] list before call aggregate
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
            if (Document.parse(it.json).size > 0) pipeline.add(
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
            if (Document.parse(it.json).size > 0) pipeline.add(
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
            println("Class: ${commonContainer.itemKClass.simpleName} ('$collectionName'), Aggregate pipeline:")
            println(pipeline.json)
        }
        return mongoColl.aggregate(pipeline, commonContainer.itemKClass.java)
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    // TODO: find a better func name
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
            println("Class: ${commonContainer.itemKClass.simpleName} ('$collectionName'), Aggregate pipeline:")
            println(pipeline.json)
        }
        return mongoColl.aggregate(pipeline, commonContainer.itemKClass.java)
    }

    /**
     * Builds a list of bson (pipeline) to be used in the *lookup* stage of the aggregate operation.
     *
     * Always appends the content result properties from the [fixedLookupList]
     *
     * @param lookupWrappers array of [LookupWrapper] items to extract lookup info
     * @return List<Bson>
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
     * helper function to write a bulk write list and clean the list after that
     */
    @Suppress("unused")
    fun bulkWrite(writeModels: MutableList<WriteModel<T>>, debug: Boolean = false) {
        CoroutineScope(context = Dispatchers.IO).launch {
            if (writeModels.size > 0) {
                if (debug) {
                    println("BulkWrite ${writeModels.hashCode()} start with ${writeModels.size} items.")
                }
                val r = coroutineColl.bulkWrite(writeModels)
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

    private fun checkSignatures(json: String): BsonDocument {
        val result = BsonDocument.parse(json)
        val bson = BsonDocument()
        val properties = commonContainer.itemKClass.memberProperties
        result.forEach { entry ->
            properties.find { it.name == entry.key }?.let { kProperty: KProperty1<T, *> ->
                if (!kProperty.hasAnnotation<DontPersist>()) {
                    when (kProperty.returnType.classifier) {
                        Double::class -> bson.append(
                            entry.key, when (entry.value.bsonType) {
                                BsonType.DOUBLE -> entry.value
                                BsonType.INT32 -> BsonDouble((entry.value as BsonInt32).doubleValue())
                                BsonType.INT64 -> BsonDouble((entry.value as BsonInt64).doubleValue())
                                else -> entry.value.asDouble() // 'll throw exception
                            }
                        )


                        else -> bson.append(entry.key, entry.value)
                    }
                }
            }
        }
        return bson
    }

    private var childColls: List<Pair<KProperty1<*, ID>, Coll<*, *, *, *>>>? = null
        get() {
            if (field == null) {
                val list = mutableListOf<Pair<KProperty1<*, ID>, Coll<*, *, *, *>>>()
                commonContainer.children?.invoke()?.forEach { kProperty1: KProperty1<*, ID> ->
                    val kProperty1Owner = (kProperty1 as PropertyReference1Impl).owner
                    collSet.find { coll ->
                        if (coll.commonContainer.itemKClass == kProperty1Owner) {
                            coll.commonContainer.itemKClass.memberProperties.any { it == kProperty1 }
                        } else {
                            false
                        }
                    }?.let { coll ->
                        list.add(kProperty1 to coll)
                    }
                }
                field = list
            }
            return field
        }

    /**
     * Finds the children that are not associated with the given ID.
     *
     * @param id The ID of the item to check for children.
     * @return An instance of ItemState indicating the status of the operation. If any children are found, the returned ItemState will have isOk set to false and msgError will contain
     *  the error message. If no children are found, the returned ItemState will have isOk set to true.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findChildrenNot(id: ID): ItemState<T> {
        childColls?.forEach { pair ->
            val item = pair.second.findOne(filter = pair.first eq id)
            if (item != null) {
                return ItemState(
                    isOk = false,
                    msgError = "'${commonContainer.labelItem}' has '${pair.second.commonContainer.labelList}' children"
                )
            }
        }
        return ItemState(isOk = true)
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
    @Suppress("unused")
    suspend fun deleteOne(
        apiItem: ApiItem.Action.Delete<T, ID, FILT>,
        filter: Bson? = null,
    ): ItemState<T> {
        return try {
            val itemState = findChildrenNot(apiItem.item._id)
            if (!itemState.isOk) return itemState
            onBeforeDelete(apiItem).also {
                if (!it.isOk) return ItemState(it)
            }
            val result = coroutineColl.deleteOne(
                and(
                    BaseDoc<*>::_id eq apiItem.item._id,
                    filter ?: EMPTY_BSON
                )
            ).deletedCount == 1L
            if (result) onAfterDelete(apiItem)
            ItemState(
                isOk = result,
                msgOk = "Delete operation ok"
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
    }

    /**
     * Override to build indexes
     */
    open suspend fun CoroutineCollection<T>.ensureIndexes() {
    }

    /**
     * Builds the final pipeline to be used in the db engine including defined lookups in [lookupFun]
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
     * Find [filter] expression in collection and returns a list of [T] items
     *
     * @param filter bson expression
     * @param lookupWrappers array of [LookupWrapper]
     * @return list of T items
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
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
     * Find [filter] expression in collection and returns a list of [T] items
     *
     * @param filter bson expression
     * @param lookupWrappers array of [LookupWrapper]
     * @return list of T items
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

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOneById(
        id: ID?,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
    ): T? {
        return findOne(BaseDoc<*>::_id eq id, apiFilter, lookupWrappers)
    }

    @Suppress("unused")
    suspend fun findItemState(
        apiItem: ApiItem.Query<T, ID, FILT>,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList()
    ): ItemState<T> {
        return findItemStateById(
            id = apiItem.id,
            apiFilter = apiItem.apiFilter,
            lookupWrappers = lookupWrappers,
        )
    }

    @Suppress("unused")
    suspend fun findItemStateById(
        id: ID?,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList()
    ): ItemState<T> {
        return try {
            ItemState(
                item = findOneById(id = id, apiFilter = apiFilter, lookupWrappers = lookupWrappers),
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
        apiItem = ApiItem.Action.Upsert.Create(
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
        apiItem: ApiItem.Query.Upsert.Create<T, ID, FILT>,
        item: T,
        overrideValidation: Boolean = false
    ): ItemState<T> {
        val itemState = insertOne(
            apiItem = ApiItem.Action.Upsert.Create(item, apiItem.apiFilter),
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
    @Suppress("unused")
    suspend fun insertOne(
        apiItem: ApiItem.Action.Upsert.Create<T, ID, FILT>,
        overrideValidation: Boolean = false
    ): ItemState<T> {
        val item = apiItem.item
        if (!overrideValidation) {
            commonContainer.validateItem(item = item, apiItem.apiFilter).also { itemState ->
                if (!itemState.isOk) return itemState
            }
        }
        checkDontPersist(item)
        return try {
            onBeforeUpsert(apiItem).also {
                if (!it.isOk) return ItemState(it)
            }
            val insertOneResult: InsertOneResult = mongoColl.insertOne(item).awaitSingle()
            val result = insertOneResult.insertedId != null
            if (result) onAfterUpsert(apiItem)
            ItemState(
                item = item,
                state = if (result) State.Ok else State.Error,
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
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
    suspend fun listContainer(
        listFirstStage: ListFirstStage,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
        countType: CountType = CountType.PreLookup,
        debug: Boolean? = this.debug,
        postProcessList: (suspend (List<T>) -> List<T>)? = null,
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
            println("Class: ${commonContainer.itemKClass.simpleName} ('$collectionName'), Aggregate time: ${t1}ms, Count time: ${t2}ms")
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
     * Returns a [ListState] built with the parameters provided
     **/
    @Suppress("unused")
    suspend fun listContainer(
        preLookupMatch: Bson? = null,
        postLookupMatch: Bson? = null,
        preLookupSort: Bson? = null,
        postLookupSort: Bson? = null,
        apiList: ApiList<FILT>,
        countType: CountType = CountType.PreLookup,
        debug: Boolean? = this.debug,
        lookupWrappers: List<LookupWrapper<*, *>> = emptyList(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        postProcessList: (suspend (List<T>) -> List<T>)? = null
    ): ListState<T> {
        return listContainer(
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
            if (result.size > 0) postLookupMatchList.add(and(result))
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
     * Method to be called after deleting an item from the API.
     *
     * @param apiItem The `ApiItem` containing the item that was deleted.
     */
    open suspend fun onAfterDelete(apiItem: ApiItem.Action.Delete<T, ID, FILT>) = Unit

    /**
     * Method to be called after upserting an item into the API.
     *
     * @param apiItem The `ApiItem` containing the item that was upserted.
     */
    open suspend fun onAfterUpsert(apiItem: ApiItem.Action.Upsert<T, ID, FILT>) = Unit

    /**
     * Executes before deleting an [ApiItem], and vetoes if [SimpleState] response [State] is not [State.Ok]
     *
     * @param apiItem The [ApiItem] to be deleted.
     * @return A [SimpleState] indicating the result of the operation.
     */
    open suspend fun onBeforeDelete(apiItem: ApiItem.Action.Delete<T, ID, FILT>): SimpleState =
        SimpleState(isOk = true)

    /**
     * Executes before upserting an [ApiItem], and vetoes if [SimpleState] response [State] is not [State.Ok]
     *
     * @param apiItem the API item being upserted.
     * @return a SimpleState object indicating the success or failure of the operation.
     */
    open suspend fun onBeforeUpsert(apiItem: ApiItem.Action.Upsert<T, ID, FILT>): SimpleState =
        SimpleState(isOk = true)

    /**
     * Allows to build a custom pipeline to be added to the [buildPipeline] in the db engine call
     */
    open suspend fun refactorPipeline(
        pipeline: MutableList<Bson> = mutableListOf(),
        resultUnit: ResultUnit,
        apiFilter: FILT = commonContainer.apiFilterInstance(),
    ): MutableList<Bson> = pipeline

    /**
     * Updates a single item in the database based on the provided filter.
     *
     * @param apiItem The API item containing the item id [ApiItem] to update and other relevant information.
     * @param filter The filter to apply when updating the item. If null, no filter will be applied.
     * @param updateOptions The options to use when updating the item.
     *
     * @return The state of the item after the update operation.
     */
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    suspend fun updateOne(
        apiItem: ApiItem.Action.Upsert.Update<T, ID, FILT>,
        filter: Bson? = null,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemState<T> {
        val item = apiItem.item
        onBeforeUpsert(apiItem).also {
            if (!it.isOk) return ItemState(it)
        }
        commonContainer.validateItem(item = item, apiFilter = apiItem.apiFilter).also { itemState ->
            if (!itemState.isOk) return itemState
        }
        checkDontPersist(item)
        val filter1 = and(BaseDoc<ID>::_id eq item._id, filter ?: EMPTY_BSON)
        val updateResult = try {
            mongoColl.coroutine.updateOne(
                filter = filter1,
                target = item,
                options = updateOptions
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ItemState(
                isOk = false,
                msgError = e.message
            )
        }
        return if (updateResult.matchedCount > 0) {
            onAfterUpsert(apiItem)
            ItemState(
                state = if (updateResult.matchedCount == 1L) State.Ok else State.Warn,
                noDataModified = updateResult.modifiedCount == 0L,
                msgError = "No data was modified ..."
            )
        } else {
            ItemState(
                isOk = false,
                msgError = "${commonContainer.labelItemId(item)} not found with [ ${filter1.json} ]"
            )
        }
    }

    init {
        @Suppress("LeakingThis")
        collSet.add(this)
        CoroutineScope(Dispatchers.IO).launch {
            with(coroutineColl) {
                ensureIndexes()
            }
        }
    }

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
                            if (Document.parse(match.json).size > 0)
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
