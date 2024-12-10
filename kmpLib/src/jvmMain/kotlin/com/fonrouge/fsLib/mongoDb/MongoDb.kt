package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.serializers.*
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.reactivestreams.client.MongoDatabase
import io.ktor.server.application.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerModule
import java.util.*

internal var mongoDbPluginConfiguration: MongoDbPluginConfiguration = MongoDbPluginConfiguration()

/**
 * Lazily initialized variable to hold the MongoDB client instance.
 *
 * This variable uses the KMongo library to create a MongoDB client based
 * on the provided connection string from the mongoDbPluginConfiguration.
 */
val mongoClient by lazy {
    mongoDbPluginConfiguration.let { KMongo.createClient(it.connectionString) }
}

/**
 * A lazily initialized instance of [MongoDatabase] that represents the MongoDB database
 * specified in the MongoDB plugin configuration.
 *
 * This database instance is obtained from the [mongoClient] using the database name
 * provided by [mongoDbPluginConfiguration.database].
 *
 * The [MongoDatabase] instance is initialized only once when accessed for the first time,
 * and subsequent accesses return the same instance.
 */
val mongoDatabase: MongoDatabase by lazy {
    mongoClient.getDatabase(mongoDbPluginConfiguration.database)
}

/**
 * Creates a `Collation` object with the specified locale and collation strength.
 *
 * @param locale The locale to be used for collation. Defaults to the locale specified in mongoDbPluginConfiguration.
 * @param collationStrength The strength to be used for collation. Defaults to `CollationStrength.PRIMARY`.
 * @return A `Collation` object configured with the given locale and collation strength.
 */
fun collation(
    locale: String = mongoDbPluginConfiguration.locale,
    collationStrength: CollationStrength = CollationStrength.PRIMARY
): Collation {
    return Collation.builder().locale(locale).collationStrength(collationStrength).build()
}

/**
 * MongoDbPlugin is an application plugin for integrating MongoDB with the application.
 * It sets up the required configuration for connecting to a MongoDB database.
 *
 * The plugin uses the `MongoDbPluginConfiguration` class to gather necessary configurations
 * like server details, authentication information, and the target database.
 *
 * The plugin outputs a confirmation message when it is successfully installed.
 */
@Suppress("unused")
val MongoDbPlugin = createApplicationPlugin(
    name = "MongoDbPlugin",
    createConfiguration = ::MongoDbPluginConfiguration
) {
    mongoDbPluginConfiguration = pluginConfig
    println("MongoDbPlugin is installed: host=${mongoDbPluginConfiguration.serverUrl}, database=${mongoDbPluginConfiguration.database}")
}

// TODO: remove declaration redundancy by add this to KMongo
val serializersModule
    get() = SerializersModule {
        contextual(OIdSerializer)
        contextual(StringIdSerializer)
        contextual(IntIdSerializer)
        contextual(LongIdSerializer)
        contextual(FSOffsetDateTimeSerializer)
        contextual(FSLocalDateSerializer)
        contextual(FSLocalDateTimeSerializer)
    }

/**
 * This class provides configuration settings for the MongoDB plugin.
 * It includes properties for specifying server details, authentication information,
 * the target database, and locale settings.
 *
 * @property serverUrl The URL of the MongoDB server. Defaults to "localhost".
 * @property serverPort The port number on which the MongoDB server is running. Defaults to 27017.
 * @property authSource The database to use for authentication. Defaults to null.
 * @property user The username for authentication. Defaults to null.
 * @property password The password for authentication. Defaults to null.
 * @property database The name of the database to connect to. Defaults to "test".
 * @property locale The locale setting to use. Defaults to the default locale language.
 * @property connectionString The constructed MongoDB connection string based on the provided configurations.
 */
class MongoDbPluginConfiguration {
    var serverUrl: String? = "localhost"
    var serverPort: Int = 27017
    var authSource: String? = null
    var user: String? = null
    var password: String? = null
    var database: String = "test"
    var locale: String = Locale.getDefault().language

    val connectionString
        get() = "mongodb://" + if (user != null || password != null) {
            "$user:$password@"
        } else {
            ""
        }.let {
            "$it$serverUrl:$serverPort" + if (authSource != null) "/?authSource=$authSource" else ""
        }

    init {
        registerModule(serializersModule)
    }
}
