package com.fonrouge.fsLib.mongoDb

import com.mongodb.MongoNamespace
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.WriteModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.reactive.asFlow
import org.bson.BsonObjectId
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import java.util.*

@Suppress("unused")
suspend fun fixToObjectId(coll: Coll<*, *>): Boolean {
    val collName = coll.collectionName
    val newCollName = "new$collName"
    if (collName !in mongoDatabase.listCollectionNames().toList()) return false
    if (newCollName in mongoDatabase.listCollectionNames().toList()) return false
    val documentColl = mongoDatabase.getCollection(collName)
    val list = mutableListOf<WriteModel<Document>>()
    documentColl.find().asFlow().collect { document ->
        when (val id = document["_id"]) {
            is String -> {
                document["_id"] = when (id.length) {
                    24 -> {
                        ObjectId(id)
                    }

                    else -> {
                        ObjectId(Base64.getUrlDecoder().decode(document.getString("_id")))
                    }
                }.let { objectId -> BsonObjectId(objectId) }
                list.add(InsertOneModel(document))
            }
        }
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
