package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.annotations.DontPersist
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.model.base.IAppUser
import com.mongodb.reactivestreams.client.AggregatePublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.ktor.http.*
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

abstract class CTableDb<T : BaseModel<U>, U>(
    private val klass: KClass<T>,
    var debug: Boolean? = null
) {
    companion object {
        var globalDebug = false
        var appUsersCollectionName = "__appUsers"
        internal val map1 = mutableMapOf<KClass<*>, CTableDb<*, *>>()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val collectionName =
        if (klass.isSubclassOf(IAppUser::class)) appUsersCollectionName
        else klass.findAnnotation<MongoDoc>()?.collection ?: klass.simpleName!!

    /**
     * [List] of [Bson] that is *always* added in the [buildLookup] function
     * for the aggregation operation
     */
    var constPipelineList: List<Bson>? = null
    var lookup: List<LookupBuilder<T, *, *, *>>? = null
        get() {
            if (field == null) {
                field = lookupFun?.invoke() ?: listOf()
            }
            return field
        }
    open val lookupFun: (() -> List<LookupBuilder<T, *, *, *>>)? = null
    val mongoColl: MongoCollection<T> = mongoDatabase.getCollection(collectionName, klass.java)

    @Suppress("unused")
    val coroutineColl = mongoColl.coroutine

    /**
     * build an AggregatePublisher<T>.
     *
     * Is the base for the find(), findOne(), findOneById()
     * accept a custom pipeline (list of Bson) argument and
     * also accept a list of ModelLookup to be added to the
     * final pipelina on the aggregate operation
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun aggregate(
        pipeline: List<Bson>? = null,
        vararg modelLookup: ModelLookup<*, *>
    ): AggregatePublisher<T> {
        val pip1 = mutableListOf<Bson>()
        pipeline?.let { pip1.addAll(it) }
        pip1.addAll(buildLookup(*modelLookup))
        if (debug ?: globalDebug) {
            println("Class: ${klass.simpleName}, Aggregate:")
            println(pip1.json)
        }
        return mongoColl.aggregate(pip1, klass.java)
    }

    /**
     * Builds a list of bson (pipeline) to be used in the *lookup* stage of the aggregate operation.
     *
     * Accepts a list of ModelLookup and appends the content of [constPipelineList]
     *
     * @param modelLookup array of ModelLookup items
     * @return List<Bson>
     */
    fun buildLookup(vararg modelLookup: ModelLookup<*, *>): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookup?.forEach { lookupBuilder ->
            modelLookup.firstOrNull { lookupBuilder.resultProperty == it.resultProperty }
                ?.let { modelLookup: ModelLookup<*, *> ->
                    lookupBuilder.addToPipeline(pipeline, modelLookup)
                }
        }
        constPipelineList?.forEach {
            pipeline.add(it)
        }
        return pipeline
    }

    private fun checkDontPersist(item: T) {
        item::class.memberProperties.forEach { kProperty1 ->
            if (kProperty1.hasAnnotation<DontPersist>() && kProperty1 is KMutableProperty1) {
                kProperty1.setter.call(item, null)
            }
        }
    }

    @Suppress("unused")
    suspend fun deleteOneById(_id: U?): ItemContainer<T> {
        if (_id != null) {
            return try {
                ItemContainer(
                    isOk = mongoColl
                        .deleteOne(BaseModel<*>::_id eq _id)
                        .awaitFirstOrNull()?.deletedCount == 1L
                )
            } catch (e: Exception) {
                ItemContainer(isOk = false, msgError = e.message)
            }
        }
        return ItemContainer(isOk = false, msgError = "_id not valid ...")
    }

    /**
     * Find [bson] expression in collection and return a list of [T] items
     *
     * @param bson bson expression
     * @param modelLookup array of ModelLookup
     * @return list of T items
     */
    suspend fun find(
        bson: Bson? = null,
        vararg modelLookup: ModelLookup<*, *>
    ): List<T> {
        return aggregate(bson?.let { listOf(match(bson)) }, *modelLookup).toList()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun findOne(
        bson: Bson? = null,
        vararg modelLookup: ModelLookup<*, *>
    ): T? {
        return aggregate(bson?.let { listOf(match(bson)) }, *modelLookup).awaitFirstOrNull()
    }

    @Suppress("unused")
    suspend fun findOneById(
        _id: U?,
        vararg modelLookup: ModelLookup<*, *>
    ): T? {
        return findOne(BaseModel<*>::_id eq _id, *modelLookup)
    }

    @Suppress("unused")
    suspend fun getItemContainer(
        _id: U?,
        vararg modelLookup: ModelLookup<*, *>
    ): ItemContainer<T> {
        return try {
            ItemContainer(
                item = findOneById(_id = _id, modelLookup = modelLookup)
            )
        } catch (e: Exception) {
            ItemContainer(isOk = false, msgError = e.message)
        }
    }

    @Suppress("unused")
    suspend fun insertOne(state: StateItem<T>): ItemContainer<T> {
        state.item?.let {
            checkDontPersist(it)
            try {
                val insertOneResult = mongoColl.insertOne(it).awaitFirstOrNull()
                val result = insertOneResult?.insertedId != null
                return ItemContainer(
                    item = it,
                    isOk = result,
                    itemAlreadyOn = result &&
                            state.callType == StateItem.CallType.Query &&
                            state.crudAction == CrudAction.Create
                )
            } catch (e: Exception) {
                return ItemContainer(isOk = false, msgError = e.message)
            }
        }
        return ItemContainer(isOk = false, msgError = "insertOne(): item contains null value...")
    }

    @Suppress("unused")
    suspend fun listFirstStage(
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
        other: List<Bson>? = null,
    ): FirstStage {
        val pipeline = mutableListOf<Bson>()
        val matchDocument = match?.toBsonDocument()?.get("\$match")?.asDocument() ?: match?.toBsonDocument()
        val filterDocument = if (!filter.isNullOrEmpty()) {
            val bdoc = BsonDocument()
            val kProperty1s = klass.memberProperties
            filter.forEach { remoteFilter ->
                val kfield = kProperty1s.firstOrNull { it.name == remoteFilter.field }
                val value: BsonValue? = when (kfield?.returnType?.classifier) {
                    String::class, null -> {
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
        val count = mongoColl.countDocuments(and(matchDocument, filterDocument)).awaitFirstOrNull() ?: 0L
        if (page == null) {
            matchDocument?.let { pipeline.add(match(matchDocument)) }
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
            matchDocument?.let { pipeline.add(match(matchDocument)) }
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

    @Suppress("unused")
    suspend fun remoteData(
        firstStage: FirstStage,
        vararg modelLookup: ModelLookup<*, *>
    ): RemoteData<T> {
        return RemoteData(
            data = aggregate(firstStage.pipeline, *modelLookup).toList(),
            last_page = firstStage.last_page,
            last_row = firstStage.last_row,
        )
    }

    /**
     * Returns a [RemoteData] builded with the parameters provided
     *
     * @param other is an optional Bson list to be added at *end* of builded pipeline
     */
    @Suppress("unused")
    suspend fun remoteData(
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
        other: List<Bson>? = null,
        vararg modelLookup: ModelLookup<*, *>
    ): RemoteData<T> {
        return remoteData(
            firstStage = listFirstStage(
                match = match,
                sort = sort,
                page = page,
                size = size,
                filter = filter,
                sorter = sorter,
                other = other,
            ),
            *modelLookup
        )
    }

    @Suppress("unused")
    suspend fun updateOne(_id: U?, state: StateItem<T>): ItemContainer<T> {
        try {
            state.item?.let {
                checkDontPersist(it)
                val result = mongoColl.coroutine.updateOne(
                    filter = it::_id eq _id,
                    target = it
                )
                return ItemContainer(isOk = result.modifiedCount == 1L)
            }
            state.json?.let {
                val result = mongoColl.coroutine.updateOne(
                    filter = BaseModel<*>::_id eq _id,
                    update = BsonDocument("\$set", BsonDocument.parse(it))
                )
                return ItemContainer(isOk = result.modifiedCount == 1L)
            }
        } catch (e: Exception) {
            return ItemContainer(isOk = false, msgError = e.message)
        }
        return ItemContainer(isOk = false, msgError = "Invalid data on StateItem ...")
    }

    init {
        @Suppress("LeakingThis")
        map1[this::class] = this
    }
}
