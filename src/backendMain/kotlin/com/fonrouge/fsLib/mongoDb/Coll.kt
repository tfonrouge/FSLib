package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.model.*
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

abstract class Coll<T : BaseDoc<ID>, ID : Any, FILT : Any>(
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
    var constLookupList: List<KProperty1<in T, *>>? = null
    open val lookupFun: ((FILT?) -> List<LookupPipelineBuilder<T, *, *>>)? = null
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
        lookups: Array<out LookupWrapper<*, *>> = emptyArray(),
        apiFilter: FILT? = null,
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
    ): AggregatePublisher<T> {
        val pip1 = buildPipeline(pipeline, lookups, apiFilter)
        postProcessPipeline?.let { it(pip1) }
        if (debug ?: globalDebug) {
            println("Class: ${klass.simpleName}, Aggregate:")
            println(pip1.json)
        }
        return mongoColl.aggregate(pip1, klass.java)
    }

    /**
     * Builds a list of bson (pipeline) to be used in the *lookup* stage of the aggregate operation.
     *
     * Always appends the content result properties from the [constLookupList]
     *
     * @param lookupWrappers array of [LookupWrapper] items to extract lookup info
     * @return List<Bson>
     */
    fun buildLookupList(
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
        apiFilter: FILT? = null,
    ): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        val lookupPipelineBuilders = lookupFun?.invoke(apiFilter) // lookupPipelineBuilderList?.toMutableList()
            ?.plus(lookupWrappers.mapNotNull { if (it is LookupByPipeline<*, *, *>) it.pipeline else null })
        lookupPipelineBuilders?.forEach { lookupPipelineBuilder ->
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
                constLookupList?.find { kProperty1 -> kProperty1 == lookupPipelineBuilder.resultProperty }?.let {
                    pipeline += lookupPipelineBuilder.pipelineList()
                }
            }
        }
        return pipeline
    }

    /**
     * Builds the aggregation pipeline, including lookups defined with [LookupWrapper] lists
     *
     * The resulting pipeline (a [Bson] list) is build in the form:
     * [pipeline] + [lookupWrappers] (parsed from [buildLookupList] function)
     *
     * @param pipeline the pipeline passed to the aggregation function
     * @param lookupWrappers array of [LookupWrapper] that will be added to the final pipeline
     */
    open fun buildPipeline(
        pipeline: MutableList<Bson>,
        lookupWrappers: Array<out LookupWrapper<*, *>>,
        apiFilter: FILT? = null,
    ): MutableList<Bson> {
        pipeline.addAll(buildLookupList(lookupWrappers = lookupWrappers, apiFilter = apiFilter))
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
     * Find [filter] expression in collection and returns a list of [T] items
     *
     * @param filter bson expression
     * @param lookupWrappers array of [LookupWrapper]
     * @return list of T items
     */
    @Suppress("unused")
    suspend fun find(
        filter: Bson? = null,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
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
    suspend fun insertOne(apiItem: ApiItem<T>): ItemState<T> {
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
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        strictCounter: Boolean = true,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
        other: List<Bson>? = null,
    ): FirstStage {
        val pipeline = mutableListOf<Bson>()
        match?.toString()?.let {
            // TODO: add OffsetDateTime codec to avoid error using it on match()
            if (it.contains("\$match")) throw Exception("Don't use match() function ...")
        }
//        val matchDocument = match?.toBsonDocument()?.get("\$match")?.asDocument() ?: match?.toBsonDocument()
        val filterDocument = if (!filter.isNullOrEmpty()) {
            val bdoc = BsonDocument()
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
                value?.let { bdoc.append(remoteFilter.field, value) }
            }
            bdoc
        } else null
        var sortDocument: BsonDocument? = null
        if (sort != null) {
            sortDocument = sort.toBsonDocument()?.get("\$sort")?.asDocument() ?: sort.toBsonDocument()
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
            mongoColl.countDocuments(and(match, filterDocument)).awaitFirstOrNull() ?: 0L
        } else {
            mongoColl.estimatedDocumentCount().awaitFirstOrNull() ?: 0L
        }
        if (page == null) {
            match?.let { pipeline.add(match(match)) }
            filterDocument?.let { pipeline.add(match(filterDocument)) }
            sortDocument?.let { pipeline.add(sort(sortDocument)) }
            other?.let { pipeline.addAll(it) }
            return FirstStage(
                pipeline = pipeline,
                count = count,
                last_page = -1,
                last_row = null,
            )
        } else {
            require(size == null || size > 0)
            val nSize = size ?: 10
            val maxPage = ((count / nSize) + if ((count % nSize) > 0) 1 else 0).toInt()
            val nPage = kotlin.math.min(maxPage, page)
            val nSkip = nSize * (nPage - 1)
            match?.let { pipeline.add(match(match)) }
            filterDocument?.let { pipeline.add(match(filterDocument)) }
            sortDocument?.let { pipeline.add(sort(sortDocument)) }
            kotlin.math.max(nSkip, 0).let { if (it > 0) pipeline.add(skip(it)) }
            pipeline.add(limit(nSize))
            other?.let { pipeline.addAll(it) }
            return FirstStage(
                pipeline = pipeline,
                count = count,
                last_page = maxPage,
                last_row = null,
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
        postProcessList: ((List<T>) -> Unit)? = null,
        apiFilter: FILT? = null,
        noContentHashCode: Boolean = false,
    ): ListState<T> {
        val list = aggregateLookup(
            pipeline = firstStage.pipeline,
            lookups = lookupWrappers,
            apiFilter = apiFilter,
            postProcessPipeline = postProcessPipeline,
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
     *
     * @param other is an optional Bson list to be added at *end* of builded pipeline
     */
    @Suppress("unused")
    suspend fun listContainer(
        match: Bson? = null,
        sort: Bson? = null,
        strictCounter: Boolean = true,
        apiList: ApiList?,
        apiFilter: FILT? = null,
        other: List<Bson>? = null,
        lookupWrappers: Array<out LookupWrapper<*, *>> = emptyArray(),
        postProcessPipeline: ((MutableList<Bson>) -> Unit)? = null,
        preprocessList: ((List<T>) -> Unit)? = null,
        noContentHashCode: Boolean = false
    ): ListState<T> {
        return listContainer(
            firstStage = listFirstStage(
                match = match,
                sort = sort,
                page = apiList?.tabPage,
                size = apiList?.tabSize,
                strictCounter = strictCounter,
                filter = apiList?.tabFilter,
                sorter = apiList?.tabSorter,
                other = other,
            ),
            lookupWrappers = lookupWrappers,
            postProcessPipeline = postProcessPipeline,
            postProcessList = preprocessList,
            apiFilter = apiFilter,
            noContentHashCode = noContentHashCode
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun updateOne(
        filter: Bson,
        apiItem: ApiItem<T>,
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
        apiItem: ApiItem<T>,
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
