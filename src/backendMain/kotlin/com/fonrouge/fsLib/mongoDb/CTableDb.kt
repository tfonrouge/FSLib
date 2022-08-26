package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.ktor.http.*
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class CTableDb<T : BaseModel<U>, U : Any>(
    klass: KClass<T>,
) {
    private val collName = klass.findAnnotation<MongoDoc>()?.collection ?: klass.simpleName!!
    val collection: CoroutineCollection<T> = mongoDatabase.getCollection(collName, klass.java).coroutine
    open val lookupFun: (() -> List<LookupBuilder<T, *, *, *>>)? = null
    var lookup: List<LookupBuilder<T, *, *, *>>? = null
        get() {
            if (field == null) {
                field = lookupFun?.invoke() ?: listOf()
            }
            return field
        }

    companion object {
        val map1 = mutableMapOf<KClass<*>, CTableDb<*, *>>()
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> listFirstStage(
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
    ): FirstStage {
        val pipeline = mutableListOf<Bson>()
        val matchDocument = match?.toBsonDocument()?.get("\$match")?.asDocument() ?: match?.toBsonDocument()
        val filterDocument = if (!filter.isNullOrEmpty()) {
            val bdoc = BsonDocument()
            val kProperty1s = R::class.memberProperties
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
        val count: Long = collection.countDocuments(and(matchDocument, filterDocument))
        if (page == null) {
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
            return FirstStage(
                pipeline = pipeline,
                count = count,
                last_page = maxPage,
                last_row = null,
            )
        }
    }

    fun buildLookup(vararg modelLookup: ModelLookup<*, *>): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookup?.forEach { lookupBuilder ->
            modelLookup.firstOrNull { lookupBuilder.resultProperty == it.resultProperty }
                ?.let { modelLookup: ModelLookup<*, *> ->
                    lookupBuilder.addToPipeline(pipeline, modelLookup)
                }
        }
        return pipeline
    }

    @Suppress("unused")
    suspend fun deleteOneById(_id: U?): ItemContainer<T> {
        if (_id != null) {
            return ItemContainer(result = collection.deleteOneById(_id).deletedCount == 1L)
        }
        return ItemContainer(result = false)
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> findOneById(
        _id: U?,
        vararg modelLookup: ModelLookup<*, *>
    ): R? {
        val pipeline = mutableListOf(match(BaseModel<*>::_id eq _id))
        pipeline.addAll(buildLookup(*modelLookup))
        return collection.aggregate<R>(pipeline).first()
    }


    @Suppress("unused")
    suspend inline fun <reified R : T> getItemContainer(
        _id: U?,
        vararg modelLookup: ModelLookup<*, *>
    ): ItemContainer<R> {
        return ItemContainer(
            item = findOneById(_id = _id, modelLookup = modelLookup)
        )
    }

    @Suppress("unused")
    suspend fun insertOne(state: StateItem<T>): ItemContainer<T> {
        state.item?.let {
            val insertOneResult = collection.insertOne(it)
            val result = insertOneResult.insertedId != null
            return ItemContainer(
                item = it,
                result = result,
                itemAlreadyOn = result &&
                        state.callType == StateItem.CallType.Query &&
                        state.crudAction == CrudAction.Create
            )
        }
        return ItemContainer(result = false, description = "insertOne(): item contains null value...")
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> remoteData(
        firstStage: FirstStage,
        vararg modelLookup: ModelLookup<*, *>
    ): RemoteData<R> {
        firstStage.pipeline.addAll(buildLookup(*modelLookup))
        println("Aggregate = ${firstStage.pipeline.json}")
        val list = collection.aggregate<R>(firstStage.pipeline).toList()
        return RemoteData(
            data = list,
            last_page = firstStage.last_page,
            last_row = firstStage.last_row,
        )
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> remoteData(
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
        vararg modelLookup: ModelLookup<*, *>
    ): RemoteData<R> {
        return remoteData(
            firstStage = listFirstStage<R>(
                match = match,
                sort = sort,
                page = page,
                size = size,
                filter = filter,
                sorter = sorter,
            ),
            *modelLookup
        )
    }

    @Suppress("unused")
    suspend fun updateOne(_id: U?, state: StateItem<T>): ItemContainer<T> {
        state.item?.let {
            val result = collection.updateOne(
                filter = it::_id eq _id,
                target = it
            )
            return ItemContainer(result = result.modifiedCount == 1L)
        }
        state.json?.let {
            val result = collection.updateOne(
                filter = BaseModel<*>::_id eq _id,
                update = BsonDocument("\$set", BsonDocument.parse(it))
            )
            return ItemContainer(result = result.modifiedCount == 1L)
        }
        return ItemContainer(result = false, description = "Invalid data on StateItem ...")
    }

    init {
        @Suppress("LeakingThis")
        map1[this::class] = this
    }
}
