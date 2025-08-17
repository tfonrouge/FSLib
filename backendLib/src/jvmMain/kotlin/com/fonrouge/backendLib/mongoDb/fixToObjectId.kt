package com.fonrouge.backendLib.mongoDb

import com.fonrouge.fsLib.model.BaseDoc
import com.mongodb.MongoNamespace
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.WriteModel
import kotlinx.coroutines.reactive.asFlow
import org.bson.BsonObjectId
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Converts string IDs in documents to ObjectId format if applicable,
 * creates a new collection with modified documents, and renames the new collection to the original name.
 *
 * @param baseDocKClass KClass of the document type extending BaseDoc<*>.
 * @param fields List of pairs where each pair consists of a KProperty1 reference of the document and an optional transformation function that takes a Document and returns Unit.
 * @return Boolean indicating whether the operation was successful.
 */
@Suppress("unused")
suspend fun <T : BaseDoc<*>> fixToObjectId(
    baseDocKClass: KClass<T>,
    fields: List<Pair<KProperty1<T, *>, ((Document) -> Unit)?>>,
): Boolean {
    val collName = baseDocKClass.collectionName
    val newCollName = "${collName}_new"
    if (collName !in mongoDatabase.listCollectionNames().toList()) return false
    if (newCollName in mongoDatabase.listCollectionNames().toList()) return false
    val documentColl = mongoDatabase.getCollection(collName)
    val list = mutableListOf<WriteModel<Document>>()
    val fieldNames = fields.map { it.first.name to it.second }
    documentColl.find().asFlow().collect { document ->
        fieldNames.forEach { (fieldName, block) ->
            block?.let {
                document[fieldName] = it(document)
            } ?: run {
                when (val id = document[fieldName]) {
                    is String -> {
                        document[fieldName] = when (id.length) {
                            24 -> {
                                ObjectId(id)
                            }

                            else -> {
                                try {
                                    ObjectId(
                                        Base64.getUrlDecoder().decode(document.getString(fieldName))
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }
                        }?.let { objectId ->
                            BsonObjectId(objectId)
                        }
                    }
                }
            }
        }
        list.add(InsertOneModel(document))
    }
    if (list.isNotEmpty()) {
        val newColl = mongoDatabase.getCollection(newCollName)
        val r = newColl.coroutine.bulkWrite(list)
        println("*** Conversion of collection '$collName' inserted documents = ${r.insertedCount}")
        mongoDatabase.coroutine.dropCollection(collName)
        mongoDatabase.getCollection(newCollName)
            .renameCollection(MongoNamespace(mongoDatabase.name, collName))
            .asFlow()
            .collect {
                println(it)
            }
    }
    return true
}
