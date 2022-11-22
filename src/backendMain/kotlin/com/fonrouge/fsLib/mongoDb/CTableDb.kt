package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemResponse
import com.fonrouge.fsLib.model.ListContainer
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.model.base.ISysUser
import com.mongodb.client.model.UpdateOptions
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.ktor.http.*
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import java.util.zip.CRC32
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

abstract class CTableDb<T : BaseModel<U>, U : Any>(
    private val klass: KClass<T>,
    var debug: Boolean? = null
) {
    companion object {
        var globalDebug = false
        internal val map1 = mutableMapOf<KClass<*>, CTableDb<*, *>>()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val collectionName =
        if (klass.isSubclassOf(ISysUser::class)) mongoDbPluginConfiguration.sysUsersCollectionName
        else klass.findAnnotation<MongoDoc>()?.collection ?: klass.simpleName!!

    /**
     * [List] of [Bson] (lookup result properties) that is *always* added in the [buildLookupList] function
     * for the aggregation operation
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var constLookupList: List<KProperty1<T, *>>? = null
    var lookupPipelineBuilderList: List<LookupPipelineBuilder<T, *, *>>? = null
        get() {
            if (field == null) {
                field = lookupFun?.invoke() ?: listOf()
            }
            return field
        }
    open val lookupFun: (() -> List<LookupPipelineBuilder<T, *, *>>)? = null
    val mongoColl: MongoCollection<T> = mongoDatabase.getCollection(collectionName, klass.java)

    @Suppress("unused")
    val coroutineColl = mongoColl.coroutine

    /**
     * build an AggregatePublisher<T>.
     *
     * Is the base for the find(), findOne(), findOneById()
     * accept a custom pipeline (list of Bson) argument and
     * also accept a list of ModelLookup to be added to the
     * final pipeline on the aggregate operation
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun aggregate(
        pipeline: MutableList<Bson>,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): AggregatePublisher<T> {
        val pip1 = buildPipeline(pipeline, modelLookups)
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
     * @param arrayOfModelLookups array of ModelLookup items to extract lookup info
     * @return List<Bson>
     */
    fun buildLookupList(arrayOfModelLookups: Array<out ModelLookup<*, *>> = emptyArray()): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookupPipelineBuilderList?.forEach { lookupPipelineBuilder ->
            val modelLookup = arrayOfModelLookups.find { lookupPipelineBuilder.resultProperty == it.resultProperty }
            if (modelLookup != null) {
                pipeline += lookupPipelineBuilder.pipelineList(modelLookup)
            } else {
                constLookupList?.find { kProperty1 -> kProperty1 == lookupPipelineBuilder.resultProperty }?.let {
                    pipeline += lookupPipelineBuilder.pipelineList()
                }
            }
        }
        return pipeline
    }

    /**
     * Builds the aggregation pipeline, including lookups defined with [ModelLookup] lists
     *
     * The resulting pipeline (a [Bson] list) is build in the form:
     * [pipeline] + [modelLookup] (parsed from [buildLookupList] function)
     *
     * @param pipeline the pipeline passed to the aggregation function
     * @param modelLookup array of [ModelLookup] that will be added to the final pipeline
     */
    open fun buildPipeline(
        pipeline: MutableList<Bson>,
        modelLookup: Array<out ModelLookup<*, *>>
    ): List<Bson> {
        pipeline.addAll(buildLookupList(modelLookup))
        return pipeline
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

    suspend fun deleteOne(filter: Bson): ItemResponse<T> {
        return try {
            ItemResponse(
                isOk = coroutineColl.deleteOne(filter = filter).deletedCount == 1L
            )
        } catch (e: Exception) {
            ItemResponse(isOk = false, msgError = e.message)
        }
    }

    @Suppress("unused")
    suspend fun deleteOneById(_id: U?): ItemResponse<T> {
        if (_id != null) {
            return try {
                ItemResponse(
                    isOk = mongoColl
                        .deleteOne(BaseModel<*>::_id eq _id)
                        .awaitFirstOrNull()?.deletedCount == 1L
                )
            } catch (e: Exception) {
                ItemResponse(isOk = false, msgError = e.message)
            }
        }
        return ItemResponse(isOk = false, msgError = "_id not valid ...")
    }

    /**
     * Override to build indexes
     */
    @Suppress("unused")
    open suspend fun CoroutineCollection<T>.ensureIndexes() {
    }

    /**
     * Find [filter] expression in collection and returns a list of [T] items
     *
     * @param filter bson expression
     * @param modelLookups array of ModelLookup
     * @return list of T items
     */
    suspend fun find(
        filter: Bson? = null,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): List<T> {
        return aggregate(filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(), modelLookups).toList()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOne(
        filter: Bson? = null,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): T? {
        return aggregate(filter?.let { mutableListOf(match(filter)) } ?: mutableListOf(),
            modelLookups).awaitFirstOrNull()
    }

    @Suppress("unused")
    suspend fun findOneById(
        _id: U?,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): T? {
        return findOne(BaseModel<*>::_id eq _id, modelLookups)
    }

    @Suppress("unused")
    suspend fun itemResponse(
        _id: U?,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): ItemResponse<T> {
        return try {
            ItemResponse(
                item = findOneById(_id = _id, modelLookups = modelLookups)
            )
        } catch (e: Exception) {
            ItemResponse(isOk = false, msgError = e.message)
        }
    }

    // TODO: Implement collect data from [state.json]
    @Suppress("unused")
    suspend fun insertOne(state: StateItem<T>): ItemResponse<T> {
        state.item?.let {
            checkDontPersist(it)
            try {
                val insertOneResult = mongoColl.insertOne(it).awaitFirstOrNull()
                val result = insertOneResult?.insertedId != null
                return ItemResponse(
                    item = it,
                    isOk = result,
                    itemAlreadyOn = result &&
                            state.callType == StateItem.CallType.Query &&
                            state.crudAction == CrudAction.Create
                )
            } catch (e: Exception) {
                return ItemResponse(isOk = false, msgError = e.message)
            }
        }
        return ItemResponse(isOk = false, msgError = "insertOne(): state.item contains null value...")
    }

    @Suppress("unused")
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
                    Array<String>::class, String::class, null -> {
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

    @OptIn(InternalSerializationApi::class)
    @Suppress("unused")
    suspend fun listContainer(
        firstStage: FirstStage,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): ListContainer<T> {
        val list = aggregate(firstStage.pipeline, modelLookups).toList()
        val encoded = Json.encodeToString(ListSerializer(klass.serializer()), list)
        val crC32 = CRC32()
        crC32.update(encoded.toByteArray())
        return ListContainer(
            data = list,
            last_page = firstStage.last_page,
            last_row = firstStage.last_row,
            checksum = crC32.value.toString(),
        )
    }

    /**
     * Returns a [ListContainer] builded with the parameters provided
     *
     * @param other is an optional Bson list to be added at *end* of builded pipeline
     */
    @Suppress("unused")
    suspend fun listContainer(
        match: Bson? = null,
        sort: Bson? = null,
        strictCounter: Boolean = true,
        contextDataUrl: ContextDataUrl?,
        other: List<Bson>? = null,
        modelLookups: Array<out ModelLookup<*, *>> = emptyArray()
    ): ListContainer<T> {
        return listContainer(
            firstStage = listFirstStage(
                match = match,
                sort = sort,
                page = contextDataUrl?.tabPage,
                size = contextDataUrl?.tabSize,
                strictCounter = strictCounter,
                filter = contextDataUrl?.tabFilter,
                sorter = contextDataUrl?.tabSorter,
                other = other,
            ),
            modelLookups = modelLookups
        )
    }

    @Suppress("unused")
    suspend fun updateOne(
        filter: Bson,
        state: StateItem<T>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemResponse<T> {
        val msgError = "No data was modified ..."
        if (state.item != null) {
            checkDontPersist(state.item)
            val result = try {
                mongoColl.coroutine.updateOne(
                    filter = filter,
                    target = state.item,
                    options = updateOptions
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
            return ItemResponse(
                isOk = result?.matchedCount == 1L,
                noDataModified = result?.modifiedCount == 0L,
                msgError = msgError
            )
        } else if (state.json != null) {
            val result = try {
                mongoColl.coroutine.updateOne(
                    filter = filter,
                    update = BsonDocument("\$set", checkSignatures(state.json)),
                    options = updateOptions
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
            return ItemResponse(
                isOk = result?.matchedCount == 1L,
                noDataModified = result?.modifiedCount == 0L,
                msgError = msgError
            )
        }
        return ItemResponse(isOk = false, msgError = "Invalid data on StateItem ...")
    }

    @Suppress("unused")
    suspend fun updateOneById(
        _id: U?,
        state: StateItem<T>,
        updateOptions: UpdateOptions = UpdateOptions()
    ): ItemResponse<T> {
        return updateOne(
            filter = BaseModel<U>::_id eq _id,
            state = state,
            updateOptions = updateOptions
        )
    }

    init {
        @Suppress("LeakingThis")
        map1[this::class] = this
        CoroutineScope(Dispatchers.IO).launch {
            with(coroutineColl) {
                ensureIndexes()
            }
        }
    }
}
