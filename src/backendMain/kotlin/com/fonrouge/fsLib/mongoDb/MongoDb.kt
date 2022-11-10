package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.reactivestreams.client.MongoDatabase
import io.ktor.server.application.*
import org.litote.kmongo.reactivestreams.KMongo
import java.util.*

interface ITables

internal var mongoDbPluginConfiguration: MongoDbPluginConfiguration = MongoDbPluginConfiguration()

val mongoClient by lazy {
    mongoDbPluginConfiguration.let { KMongo.createClient(it.connectionString) }
}

@Suppress("unused")
val mongoDatabase: MongoDatabase by lazy {
    mongoClient.getDatabase(mongoDbPluginConfiguration.database)
}

@Suppress("unused")
fun collation(
    locale: String = mongoDbPluginConfiguration.locale,
    collationStrength: CollationStrength = CollationStrength.PRIMARY
): Collation {
    return Collation.builder().locale(locale).collationStrength(collationStrength).build()
}

@Suppress("unused")
val MongoDbPlugin = createApplicationPlugin(
    name = "MongoDbPlugin",
    createConfiguration = ::MongoDbPluginConfiguration
) {
    mongoDbPluginConfiguration = pluginConfig
    println("MongoDbPlugin is installed: host=${mongoDbPluginConfiguration.serverUrl}, database=${mongoDbPluginConfiguration.database}")
}

class MongoDbPluginConfiguration {
    var serverUrl: String? = "localhost"
    var serverPort: Int = 27017
    var authSource: String? = null
    var user: String? = null
    var password: String? = null
    var database: String = "test"
    var locale: String = Locale.getDefault().language

    /**
     * Name of collection for implementation of ISysUser, any collection name given to implemented
     * subclasses will be ignored
     */
    var sysUsersCollectionName = "__sysUsers"

    val connectionString
        get() = "mongodb://" + if (user != null || password != null) {
            "$user:$password@"
        } else {
            ""
        }.let {
            "$it$serverUrl:$serverPort" + if (authSource != null) "/?authSource=$authSource" else ""
        }
}
