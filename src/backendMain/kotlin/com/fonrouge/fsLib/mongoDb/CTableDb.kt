package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.ItemContainer
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import sun.security.krb5.internal.crypto.crc32

class CTableDb<T : BaseModel<U>, U : Any>(
    val collection: CoroutineCollection<T>,
    private val lookupBuilderList: List<LookupBuilder<T, *, *, *>>? = null,
    val genCheckSum: Boolean = false
) {

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
        match?.let {
            bsonList.add(it)
        }
        val filterValue = match?.let { bson ->
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
//            bsonList.add(Document("\$sort", fields))
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
        lookupBuilderList?.forEach { lookupBuilder ->
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
    suspend inline fun <reified R : T> findOneById(
        _id: U,
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
            match,
        )
        pipeline.addAll(buildLookup(modelLookupList))
        pipeline.add(limit(1))
        return collection.aggregate<R>(pipeline).first()
    }

    @Suppress("unused")
    suspend fun insertOne(item: T?): ItemContainer<T> {
        if (item != null) {
            val result = collection.insertOne(item)
            return ItemContainer(item = item, result = result.insertedId != null)
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
        var hashCode = 0
        if (genCheckSum) {
            list.forEach {
                hashCode += crc32.byte2crc32(Json.encodeToString(it).encodeToByteArray())
            }
        }
        return RemoteData(
            data = list,
            last_page = firstStage.last_page,
            last_row = firstStage.last_row,
//            chkSum = hashCode
        )
    }

    @Suppress("unused")
    suspend fun updateOne(_id: U?, item: T?): ItemContainer<T> {
        if (item != null) {
            val result = collection.updateOne(filter = item::_id eq _id, target = item)
            return ItemContainer(result = result.modifiedCount == 1L)
        }
        return ItemContainer(result = false, description = "updateOne(): item contains null value...")
    }

    @Suppress("unused")
    suspend fun updateOne(_id: U?, bson: Bson?): ItemContainer<T> {
        if (bson != null) {
            val result = collection.updateOne(BaseModel<*>::_id eq _id, update = bson)
            return ItemContainer(result = result.modifiedCount == 1L)
        }
        return ItemContainer(result = false, description = "updateOne(): item contains null value...")
    }
}
