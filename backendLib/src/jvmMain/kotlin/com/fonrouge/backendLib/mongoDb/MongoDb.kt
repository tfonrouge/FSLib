package com.fonrouge.backendLib.mongoDb

import com.fonrouge.fsLib.serializers.*
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.reactivestreams.client.MongoDatabase
import io.ktor.server.application.*
import org.litote.kmongo.reactivestreams.KMongo.createClient
import org.litote.kmongo.serialization.registerSerializer
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

internal var mongoDbBuilder: MongoDbBuilder = MongoDbBuilder()

/**
 * Lazily initialized variable to hold the MongoDB client instance.
 *
 * This variable uses the KMongo library to create a MongoDB client based
 * on the provided connection string from the mongoDbPluginConfiguration.
 */
val mongoClient by lazy {
    createClient(mongoDbBuilder.connectionString)
}

/**
 * Represents a lazily initialized instance of the MongoDatabase.
 * This variable is used to interact with the MongoDB database specified
 * by the configuration.
 *
 * The database instance is retrieved using the MongoClient and the database name
 * configured in the mongoDbPluginConfiguration.
 */
val mongoDatabase: MongoDatabase by lazy {
    mongoClient.getDatabase(mongoDbBuilder.database)
}

/**
 * Creates a `Collation` object with the specified locale and collation strength.
 *
 * @param locale The locale to be used for collation. Defaults to the locale specified in mongoDbPluginConfiguration.
 * @param collationStrength The strength to be used for collation. Defaults to `CollationStrength.PRIMARY`.
 * @return A `Collation` object configured with the given locale and collation strength.
 */
fun collation(
    locale: String = mongoDbBuilder.locale,
    collationStrength: CollationStrength = CollationStrength.PRIMARY,
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
    createConfiguration = ::MongoDbBuilder
) {
    mongoDbBuilder = pluginConfig
    println("MongoDbPlugin is installed: host=${mongoDbBuilder.serverUrl}, database=${mongoDbBuilder.database}")
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
data class MongoDbBuilder(
    var serverUrl: String? = "localhost",
    var serverPort: Int = 27017,
    var authSource: String? = null,
    var user: String? = null,
    var password: String? = null,
    var database: String = "test",
    var locale: String = Locale.getDefault().language,
) {

    private var cachedClient: com.mongodb.reactivestreams.client.MongoClient? = null

    val connectionString
        get() = buildString {
            append("mongodb://")
            val username = user?.takeIf { it.isNotBlank() }
            val pwd = password?.takeIf { it.isNotBlank() }
            if (username != null || pwd != null) {
                val encUser = username?.let { URLEncoder.encode(it, StandardCharsets.UTF_8) } ?: ""
                val encPwd = pwd?.let { URLEncoder.encode(it, StandardCharsets.UTF_8) } ?: ""
                append(encUser)
                if (encUser.isNotEmpty() || encPwd.isNotEmpty()) append(":")
                append(encPwd)
                append("@")
            }
            val host = serverUrl ?: "localhost"
            append(host)
            append(":")
            append(serverPort)
            authSource?.takeIf { it.isNotBlank() }?.let {
                append("/?authSource=")
                append(it)
            }
        }

    fun getMongoDb(): MongoDatabase {
        val client = cachedClient ?: createClient(connectionString).also { cachedClient = it }
        return client.getDatabase(database)
    }

    companion object {
        init {
            //
            // TODO: using registerModule doesn't encode LocalDate to 'YYYY-MM-YY' as per FSLocalDateSerializer
            // val d1: java.time.LocalDate
            // d1.json -> { "$date" : "2024-09-01T00:00:00Z" }
            //
            // registerModule(serializersModule)
            registerSerializer(OIdSerializer)
            registerSerializer(StringIdSerializer)
            registerSerializer(IntIdSerializer)
            registerSerializer(LongIdSerializer)
            registerSerializer(FSOffsetDateTimeSerializer)
            registerSerializer(FSLocalDateSerializer)
            registerSerializer(FSLocalDateTimeSerializer)
        }
    }
}
