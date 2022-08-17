package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.StateItem
import com.fonrouge.fsLib.annotations.MongoDoc
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
    suspend fun listFirstStage(
        match: Bson? = null,
        sort: Bson? = null,
        page: Int? = null,
        size: Int? = null,
        filter: List<RemoteFilter>? = null,
        sorter: List<RemoteSorter>? = null,
    ): FirstStage {
        val bsonList = mutableListOf<Bson>()
        val match0 =
            if (match == null) null else if (match.toBsonDocument()["\$match"] != null) match else match(match)
        match0?.let { bsonList.add(it) }
        val filterValue = match0?.let { bson ->
            val bsonDocument = BsonDocument()
            (bson.toBsonDocument()["\$match"] as BsonDocument).forEach {
                bsonDocument.append(it.key, it.value)
            }
            bsonDocument
        } ?: BsonDocument()
        if (!filter.isNullOrEmpty()) {
            filter.forEach { remoteFilter ->
                val value: BsonValue = when (remoteFilter.type) {
                    "like" -> BsonDocument(
                        "\$regex",
                        BsonString(remoteFilter.value)
                    ).append("\$options", BsonString("i"))

                    else -> BsonString(remoteFilter.value)
                }
                filterValue.append(remoteFilter.field, value)
            }
        }
        if (sort != null) {
            if (sort.toBsonDocument()["\$sort"] != null) {
                bsonList.add(sort)
            } else {
                bsonList.add(sort(sort))
            }
        } else if (!sorter.isNullOrEmpty()) {
            val fields = BsonDocument()
            sorter.forEach { remoteSorter ->
                fields.append(
                    remoteSorter.field, when (remoteSorter.dir) {
                        "asc" -> BsonInt32(1)
                        "desc" -> BsonInt32(-1)
                        else -> BsonInt32(1)
                    }
                )
            }
            bsonList.add(sort(fields))
        }
        val count: Long = filterValue.let { collection.countDocuments(it) }
        if (page == null) {
            return FirstStage(
                pipeline = bsonList,
                count = count,
                last_page = -1,
                last_row = null,
            )
        } else {
            val nSize = size ?: 10
            val nSkip = nSize * (page - 1)
            filterValue.let {
                bsonList.add(Document("\$match", it))
            }
            bsonList.add(
                skip(nSkip)
            )
            bsonList.add(
                limit(nSize)
            )
            return FirstStage(
                pipeline = bsonList,
                count = count,
                last_page = (count / nSize + 1).toInt(),
                last_row = null,
            )
        }
    }

    fun buildLookup(modelLookupList: List<ModelLookup<*, *>>? = null): List<Bson> {
        val pipeline: MutableList<Bson> = mutableListOf()
        lookup?.forEach { lookupBuilder ->
            modelLookupList?.firstOrNull { lookupBuilder.resultProperty == it.resultProperty }
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
    suspend inline fun <reified R : T> getItemContainer(
        _id: U?,
        modelLookupList: List<ModelLookup<*, *>>? = null
    ): ItemContainer<R> {
        return ItemContainer(
            item = getItem(
                match = match(BaseModel<*>::_id eq _id),
                modelLookupList = modelLookupList
            )
        )
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> getItem(
        match: Bson,
        modelLookupList: List<ModelLookup<*, *>>? = null
    ): R? {
        val pipeline = mutableListOf(
            if (match.toBsonDocument()["\$match"] != null) match else match(match)
        )
        pipeline.addAll(buildLookup(modelLookupList))
        pipeline.add(limit(1))
        return collection.aggregate<R>(pipeline).first()
    }

    @Suppress("unused")
    suspend fun insertOne(state: StateItem<T>): ItemContainer<T> {
        state.item?.let {
            val result = collection.insertOne(it)
            return ItemContainer(item = it, result = result.insertedId != null)
        }
        return ItemContainer(result = false, description = "insertOne(): item contains null value...")
    }

    @Suppress("unused")
    suspend inline fun <reified R : T> remoteData(
        firstStage: FirstStage,
        modelLookupList: List<ModelLookup<*, *>>? = null
    ): RemoteData<R> {
        firstStage.pipeline.addAll(buildLookup(modelLookupList))
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
        modelLookupList: List<ModelLookup<*, *>>? = null
    ): RemoteData<R> {
        return remoteData(
            firstStage = listFirstStage(
                match = match,
                sort = sort,
                page = page,
                size = size,
                filter = filter,
                sorter = sorter,
            ),
            modelLookupList = modelLookupList
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
