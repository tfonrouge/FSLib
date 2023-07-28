package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.model.*
import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.ListState
import com.fonrouge.fsLib.serializers.StringId
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.ktor.http.*
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

abstract class Coll<T : BaseDoc<ID>, ID : Any, FILT : ApiFilter>(
    private val klass: KClass<T>,
    var debug: Boolean? = null
) {
    companion object {
        var globalDebug = false
        internal val collMap = mutableMapOf<KClass<*>, Coll<*, *, *>>()
        fun collectionName(klass: KClass<out BaseDoc<*>>): String =
            if (klass.isSubclassOf(ISysUser::class)) mongoDbPluginConfiguration.sysUsersCollectionName
            else klass.findAnnotation<Collection>()?.name ?: klass.simpleName!!
    }

    val collectionName = collectionName(klass)

    /**
     * [List] of [Bson] (lookup result properties) that is *always* added in the [buildLookupList] function
     * for the aggregation operation
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open fun fixedLookupList(apiFilter: FILT?): List<KProperty1<in T, *>>? = null
    open val lookupFun: ((FILT?) -> List<LookupPipelineBuilder<T, *, *>>) = { listOf() }
    open fun childCollections(): List<KClass<out Coll<*, *, *>>> = listOf()
    val mongoColl: MongoCollection<T> = mongoDatabase.getCollection(collectionName, klass.java)

    val coroutineColl = mongoColl.coroutine

    /**
     * build an AggregatePublisher<T>.
     *
     * Is the base for the find(), findOne(), findOneById()
     * accept a custom pipeline (list of Bson) argument and
     * also accept a list of [LookupWrapper] to be added to the
     * final pipeline on the aggregate operation
     *
     * @param pipeline [Bson] list
     * @param lookups list of lookups to be included [LookupWrapper]
     * @param postProcessPipeline allow to post-process the resulted [Bson] list before call aggregate
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun aggregateLookup(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookups: Array<out LookupWrapper<*, *>>? = null,
        apiFilter: FILT? = null,
        postLookupMatch: Bson? = null,
        sort: Bson? = null,
        skip: Int = 0,
        limit: Int? = null,
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
    ): AggregatePublisher<T> {
        finalPipeline(pipeline = pipeline, lookups = lookups, apiFilter = apiFilter)
        postProcessPipeline?.let { it(pipeline) }
        postLookupMatch?.let { pipeline.add(match(it)) }
        sort?.let { pipeline.add(sort(it)) }
        kotlin.math.max(skip, 0).let { if (it > 0) pipeline.add(skip(it)) }
        limit?.let { pipeline.add(limit(it)) }
        val curTime = Date().time
        if (debug ?: globalDebug) {
            println("Class: ${klass.simpleName}, Aggregate pipeline:")
            println(pipeline.json)
        }
        return mongoColl.aggregate(pipeline, klass.java).also {
            if (debug ?: globalDebug) {
                println("Class: ${klass.simpleName}, Aggregate time: ${Date().time - curTime}ms")
            }
        }
    }

    /**
     * Builds a list of bson (pipeline) to be used in the *lookup* stage of the aggregate operation.
     *
     * Always appends the content result properties from the [fixedLookupList]
     *
     * @param lookupWrappers array of [LookupWrapper] items to extract lookup info
     * @return List<Bson>
     */
    private fun buildLookupList(
        lookupWrappers: Array<out LookupWrapper<*, *>>? = null,
        apiFilter: FILT? = null,
    ): MutableList<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        val lookupPipelineBuilders =
            lookupFun(apiFilter).plus(lookupWrappers?.mapNotNull {
                if (it is LookupByPipeline<*, *, *>) it.pipeline else null
            } ?: emptyList())
        lookupPipelineBuilders.forEach { lookupPipelineBuilder ->
            val lookupWrapper = lookupWrappers?.find {
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
    suspend fun bulkWrite(writeModels: MutableList<WriteModel<T>>) {
        if (writeModels.size > 0) {
            coroutineColl.bulkWrite(writeModels)
            writeModels.clear()
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
        val properties = klass.memberProperties
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

    /**
     * Allows to build a custom pipeline to be added to the [finalPipeline] in the db engine call
     */
    open fun customPipelineItems(
        pipeline: MutableList<Bson> = mutableListOf(),
        apiFilter: FILT? = null
    ): MutableList<Bson> = pipeline

    suspend fun deleteOne(filter: Bson): ItemState<T> {
        return try {
            ItemState(
                isOk = coroutineColl.deleteOne(filter = filter).deletedCount == 1L
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
    }

    @Suppress("unused")
    suspend fun deleteOneById(id: ID?): ItemState<T> {
        if (id != null) {
            return try {
                ItemState(
                    isOk = mongoColl
                        .deleteOne(BaseDoc<*>::_id eq id)
                        .awaitFirstOrNull()?.deletedCount == 1L
                )
            } catch (e: Exception) {
                ItemState(isOk = false, msgError = e.message)
            }
        }
        return ItemState(isOk = false, msgError = "_id not valid ...")
    }

    /**
     * Override to build indexes
     */
    open suspend fun CoroutineCollection<T>.ensureIndexes() {
    }

    /**
     * Builds the final pipeline to be used in the db engine including defined lookups in [lookupFun]
     */
    fun finalPipeline(
        pipeline: MutableList<Bson> = mutableListOf(),
        lookups: Array<out LookupWrapper<*, *>>? = null,
        apiFilter: FILT? = null,
    ): MutableList<Bson> {
        pipeline.addAll(
            customPipelineItems(
                pipeline = buildLookupList(lookupWrappers = lookups, apiFilter = apiFilter),
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
    @Suppress("unused")
    suspend fun find(
        filter: Bson? = null,
        lookupWrappers: Array<out LookupWrapper<*, *>>? = null,
        apiFilter: FILT? = null,
    ): List<T> {
        return aggregateLookup(
            pipeline = filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(),
            lookups = lookupWrappers,
            apiFilter = apiFilter,
        ).toList()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOne(
        filter: Bson? = null,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
        apiFilter: FILT? = null,
    ): T? {
        return aggregateLookup(
            pipeline = filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(),
            lookups = lookupWrappers,
            apiFilter = apiFilter,
        ).awaitFirstOrNull()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOneById(
        id: ID?,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray()
    ): T? {
        return findOne(BaseDoc<*>::_id eq id, lookupWrappers)
    }

    @Suppress("unused")
    suspend fun findOneByIdResponse(
        id: ID?,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray()
    ): ItemState<T> {
        return try {
            ItemState(
                item = findOneById(id = id, lookupWrappers = lookupWrappers),
                msgError = "_id '$id' (${klass.simpleName}) not found..."
            )
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
    }

    @Suppress("unused")
    suspend fun insertOne(apiItem: ApiItem<T, FILT>): ItemState<T> {
        apiItem.item?.let {
            checkDontPersist(it)
            try {
                val insertOneResult = mongoColl.insertOne(it).awaitFirstOrNull()
                val result = insertOneResult?.insertedId != null
                return ItemState(
                    item = it,
                    isOk = result,
                    itemAlreadyOn = result &&
                            apiItem.callType == ApiItem.CallType.Query &&
                            apiItem.crudTask == CrudTask.Create
                )
            } catch (e: Exception) {
                return ItemState(isOk = false, msgError = e.message)
            }
        }
        return ItemState(isOk = false, msgError = "insertOne(): apiItem.item contains null value...")
    }

    private suspend fun listFirstStage(
        preLookupMatch: Bson? = null,
        postLookupMatch: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        strictCounter: Boolean = true,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
    ): FirstStage {
        val pipeline = mutableListOf<Bson>()
        val postLookupMatchList: MutableList<Bson> = mutableListOf()
        postLookupMatch?.let { postLookupMatchList.add(it) }
        filter?.let {
            val result = mutableListOf<Bson>()
            val kProperty1s = klass.memberProperties
            filter.forEach { remoteFilter ->
                val kfield = kProperty1s.firstOrNull { it.name == remoteFilter.field }
                val value: BsonValue? = when (kfield?.returnType?.classifier) {
                    Array<String>::class, String::class, StringId::class, null -> {
                        when (remoteFilter.type) {
                            "like" -> BsonDocument(
                                "\$regex",
                                BsonString(remoteFilter.value)
                            ).append("\$options", BsonString("i"))

                            else -> BsonString(remoteFilter.value)
                        }
                    }

                    Int::class -> remoteFilter.value?.toIntOrNull()?.let { BsonInt32(it) }
                    Long::class -> remoteFilter.value?.toLongOrNull()?.let { BsonInt64(it) }
                    Double::class -> remoteFilter.value?.toDoubleOrNull()?.let { BsonDouble(it) }
                    else -> null
                }
                value?.let {
                    result.add(BsonDocument(remoteFilter.field, value))
                }
            }
            if (result.size > 0) postLookupMatchList.add(and(result))
        }
        var sortDocument: Bson? = null
        if (sort != null) {
            sortDocument = sort
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
        val count = if (strictCounter) {
            val list = mutableListOf<Bson>()
            preLookupMatch?.let { list.add(it) }
//            postLookupMatchList.let { list.addAll(it) }
            mongoColl.countDocuments(and(list)).awaitFirstOrNull() ?: 0L
        } else {
            mongoColl.estimatedDocumentCount().awaitFirstOrNull() ?: 0L
        }
        if (page == null) {
            preLookupMatch?.let { pipeline.add(match(preLookupMatch)) }
            return FirstStage(
                pipeline = pipeline,
                count = count,
                last_page = -1,
                last_row = null,
                postLookupMatch = and(postLookupMatchList),
                sort = sortDocument,
                limit = null,
            )
        } else {
            require(size == null || size > 0)
            val nSize = size ?: 10
            val maxPage = ((count / nSize) + if ((count % nSize) > 0) 1 else 0).toInt()
            val nPage = kotlin.math.min(maxPage, page)
            val nSkip = nSize * (nPage - 1)
            preLookupMatch?.let { pipeline.add(match(preLookupMatch)) }
            return FirstStage(
                pipeline = pipeline,
                count = count,
                last_page = maxPage,
                last_row = null,
                postLookupMatch = and(postLookupMatchList),
                sort = sortDocument,
                skip = nSkip,
                limit = nSize,
            )
        }
    }

    /**
     * Builds a [ListState] back to frontend
     *
     * @param postProcessList Allows to post-process the List<[T]> before send it to the frontend
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun listContainer(
        firstStage: FirstStage,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        apiFilter: FILT,
        noContentHashCode: Boolean = false,
        postProcessList: ((List<T>) -> Unit)? = null,
    ): ListState<T> {
        val list = aggregateLookup(
            pipeline = firstStage.pipeline,
            lookups = lookupWrappers,
            apiFilter = apiFilter,
            postLookupMatch = firstStage.postLookupMatch,
            sort = firstStage.sort,
            skip = firstStage.skip,
            limit = firstStage.limit,
            postProcessPipeline = postProcessPipeline
        ).toList()
        postProcessList?.let { it(list) }
        val contentHashCode = if (!noContentHashCode) {
            (list as List<Any>).toTypedArray().contentDeepHashCode()
        } else null
        return ListState(
            data = list,
            last_page = firstStage.last_page,
            last_row = firstStage.last_row,
            contentHashCode = contentHashCode,
        )
    }

    /*
        @OptIn(InternalSerializationApi::class)
        fun calcChecksum(list: List<T>): String {
            val encoded = Json.encodeToString(ListSerializer(klass.serializer()), list)
            val crC32 = CRC32()
            crC32.update(encoded.toByteArray())
            return crC32.value.toString()
        }
    */

    /**
     * Returns a [ListState] builded with the parameters provided
     **/
    @Suppress("unused")
    suspend fun listContainer(
        preLookupMatch: Bson? = null,
        postLookupMatch: Bson? = null,
        sort: Bson? = null,
        strictCounter: Boolean = true,
        apiList: ApiList<FILT>,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        noContentHashCode: Boolean = false,
        postProcessList: ((List<T>) -> Unit)? = null
    ): ListState<T> {
        return listContainer(
            firstStage = listFirstStage(
                preLookupMatch = preLookupMatch,
                postLookupMatch = postLookupMatch,
                sort = sort,
                page = apiList.tabPage,
                size = apiList.tabSize,
                strictCounter = strictCounter,
                filter = apiList.tabFilter,
                sorter = apiList.tabSorter,
            ),
            lookupWrappers = lookupWrappers,
            postProcessPipeline = postProcessPipeline,
            apiFilter = apiList.apiFilter,
            noContentHashCode = noContentHashCode,
            postProcessList = postProcessList
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun updateOne(
        filter: Bson,
        apiItem: ApiItem<T, FILT>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemState<T> = apiItem.item?.let {
        checkDontPersist(apiItem.item)
        val result = try {
            mongoColl.coroutine.updateOne(
                filter = filter,
                target = apiItem.item,
                options = updateOptions
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
        ItemState(
            isOk = result?.matchedCount == 1L,
            noDataModified = result?.modifiedCount == 0L,
            msgError = "No data was modified ..."
        )
    } ?: ItemState(isOk = false, msgError = "Invalid data on StateItem ...")

    @Suppress("unused")
    suspend fun updateOneById(
        id: ID?,
        apiItem: ApiItem<T, FILT>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemState<T> {
        return updateOne(
            filter = BaseDoc<ID>::_id eq id,
            apiItem = apiItem,
            updateOptions = updateOptions
        )
    }

    init {
        @Suppress("LeakingThis")
        collMap[this::class] = this
        CoroutineScope(Dispatchers.IO).launch {
            with(coroutineColl) {
                ensureIndexes()
            }
        }
    }
}
