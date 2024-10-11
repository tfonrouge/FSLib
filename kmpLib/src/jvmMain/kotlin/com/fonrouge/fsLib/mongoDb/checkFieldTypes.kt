package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.LongId
import com.fonrouge.fsLib.serializers.StringId
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.collect
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.from
import org.litote.kmongo.not
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Suppress("unused")
class CheckFieldTypes<K : KClass<T>, T : BaseDoc<ID>, ID : Any>(
    private val baseDocKClass: K,
    val includeFields: List<KProperty1<T, *>>? = null,
    val excludeFields: List<KProperty1<T, *>>? = null,
) {
    private val collection: MongoCollection<Document> = mongoDatabase.getCollection(baseDocKClass.collectionName)
    private val updateList = mutableListOf<UpdateOneModel<Document>>()
    private val bucketSize = 100000
    private var count = 0L

    private suspend fun writeBlock(finish: Boolean = false): ItemState<Long> {
        return if (updateList.size >= bucketSize || (finish && updateList.size > 0)) {
            count += bucketSize
            print(".")
            val r = collection.coroutine.bulkWrite(updateList)
            r.modifiedCount
            updateList.clear()
            ItemState(state = State.Ok, item = r.modifiedCount.toLong())
        } else {
            ItemState(state = State.Warn, item = 0)
        }
    }

    private suspend fun <T : BaseDoc<ID>, ID : Any> checkNullables(
        collection: MongoCollection<Document>,
        kProperty1: KProperty1<T, *>,
    ): ItemState<Long> {
        print("searching for null values type in not nullable field '${kProperty1.name}' ... ")
        val count = collection.coroutine.countDocuments(kProperty1 eq null)
        return if (count > 0) {
            println()
            val s =
                "  ERROR: found $count NULL entries for not nullable field '${kProperty1.name}' in collection '${collection.namespace.collectionName}' !"
            println(s)
            ItemState(
                state = State.Error,
                item = count,
                msgError = s
            )
        } else {
            println("none found")
            ItemState(state = State.Ok, item = count)
        }
    }

    private suspend fun countInvalidFields(
        filter: Bson,
        kProperty1: KProperty1<T, *>,
    ): ItemState<Long> {
        val count = collection.coroutine.countDocuments(filter)
        return if (count > 0) {
            ItemState(
                state = State.Error,
                item = count,
                msgError = "  ERROR: $count entries with invalid value for field '${kProperty1.name}'"
            )
        } else {
            ItemState(state = State.Ok, item = count, msgOk = "none found")
        }
    }

    private suspend fun fixNumericFieldValue(
        filter: Bson,
        kProperty1: KProperty1<T, *>,
        fieldKClass: KClass<*>
    ): ItemState<Long> {
        val itemState = countInvalidFields(filter, kProperty1)
        if (itemState.notError) {
            return itemState
        }
        count = 0
        var modified = 0L
        collection
            .find(filter)
            .projection(Document().append("_id", 1).append(kProperty1.name, 1))
            .collect { document: Document ->
                document[kProperty1.name]?.let { entryValue ->
                    when (fieldKClass) {
                        Double::class, Float::class, Int::class, Long::class -> {
                            when (entryValue) {
                                is Double -> when (kProperty1.returnType.classifier) {
                                    Double::class, Float::class -> entryValue.toDouble()
                                    Int::class -> entryValue.toInt()
                                    Long::class -> entryValue.toLong()
                                    else -> null
                                }

                                is Long -> when (kProperty1.returnType.classifier) {
                                    Double::class, Float::class -> entryValue.toDouble()
                                    Int::class -> entryValue.toInt()
                                    Long::class -> entryValue.toLong()
                                    else -> null
                                }

                                is Int -> when (kProperty1.returnType.classifier) {
                                    Double::class, Float::class -> entryValue.toDouble()
                                    Int::class -> entryValue.toInt()
                                    Long::class -> entryValue.toLong()
                                    else -> null
                                }

                                else -> null
                            }
                        }

                        else -> null
                    }?.let { value ->
                        updateList.add(
                            UpdateOneModel(
                                Document("_id", document["_id"]),
                                Document("\$set", Document(kProperty1.name, value)),
                            )
                        )
                        writeBlock().item?.let { modified += it }
                    }
                }
            }
        writeBlock(finish = true).item?.let { modified += it }
        println(" written $count entries, modified = $modified")
        return ItemState(isOk = true)
    }

    suspend fun start(): SimpleState {
        val collection: MongoCollection<Document> = mongoDatabase.getCollection(baseDocKClass.collectionName)
        baseDocKClass.memberProperties
            .forEach { kProperty1: KProperty1<T, *> ->
                val fieldKClass = kProperty1.returnType.classifier as KClass<*>
                if (kProperty1.returnType.isMarkedNullable.not()) {
                    val simpleState = checkNullables(collection, kProperty1)
                    if (simpleState.notError.not()) {
                        return simpleState.asSimpleState
                    }
                }
                when (fieldKClass) {
                    String::class, StringId::class -> {
                        countInvalidFields(
                            filter = kProperty1 eq not(MongoOperator.type from "string"),
                            kProperty1 = kProperty1
                        )
                    }

                    OffsetDateTime::class -> {
                        countInvalidFields(
                            filter = kProperty1 eq not(MongoOperator.type from "date"),
                            kProperty1 = kProperty1
                        )
                    }

                    ObjectId::class -> {
                        countInvalidFields(
                            filter = kProperty1 eq not(MongoOperator.type from "objectId"),
                            kProperty1 = kProperty1
                        )
                    }

                    Boolean::class -> {
                        countInvalidFields(
                            filter = kProperty1 eq not(MongoOperator.type from "bool"),
                            kProperty1 = kProperty1
                        )
                    }

                    Double::class,
                    Float::class,
                    Int::class,
                    IntId::class,
                    Long::class,
                    LongId::class -> {
                        when (kProperty1.returnType.classifier) {
                            Double::class, Float::class -> "double"
                            Int::class -> "int"
                            Long::class -> "long"
                            else -> null
                        }?.let { mongoType ->
                            print("searching for not '$mongoType' type in '${baseDocKClass.simpleName}.${kProperty1.name}' field ... ")
                            fixNumericFieldValue(
                                filter = kProperty1 eq not(MongoOperator.type from mongoType),
                                kProperty1 = kProperty1,
                                fieldKClass = fieldKClass
                            ).also { itemState: ItemState<Long> ->
                                if (itemState.notError) {
                                    println(" ${itemState.msgOk}")
                                }
                            }
                        }
                    }

                    else -> null
                }?.let { itemState: ItemState<Long> ->
                    if (itemState.notError.not()) {
                        println("  ${itemState.msgError}")
                        return itemState.asSimpleState
                    }
                } ?: println("Ignored unknown type ${fieldKClass.simpleName} for field '$kProperty1.name'")
            }
        return SimpleState(isOk = true)
    }
}
