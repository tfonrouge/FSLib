package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.reactivestreams.client.MongoDatabase
import io.ktor.server.application.*
import org.litote.kmongo.reactivestreams.KMongo

var connectionString: String = "mongodb://localhost"
private var database: String = "test"

var locale = "en"

val mongoClient by lazy {
    connectionString.let { KMongo.createClient(it) }
}

val mongoDatabase: MongoDatabase by lazy {
    mongoClient.getDatabase(database)
}

val collation by lazy {
    Collation.builder().locale(locale).collationStrength(CollationStrength.PRIMARY).build()
}

val MongoDbPlugin = createApplicationPlugin(
    name = "MongoDbPlugin",
    createConfiguration = ::MongoDbPluginConfiguration
) {

}

class MongoDbPluginConfiguration {
    var serverUrl: String? = null
    var serverPort: Int = 27017
    var authSource: String? = null
    var user: String? = null
    var password: String? = null
    var database: String? = null

    val connectionString
        get() = "mongodb://" + if (user != null || password != null) {
            "$user:$password@"
        } else {
            ""
        }.let {
            "$it$serverUrl:$serverPort" + if (authSource != null) "/?authSource=$authSource" else ""
        }

}
