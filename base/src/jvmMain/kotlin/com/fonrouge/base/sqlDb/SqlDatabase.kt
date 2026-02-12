package com.fonrouge.base.sqlDb

import com.fonrouge.base.annotations.SqlField
import com.fonrouge.base.annotations.SqlIgnoreField
import com.fonrouge.base.annotations.SqlOneToOne
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.serializers.KV_DEFAULT_DATETIME_FORMAT
import com.fonrouge.base.state.SimpleState
import com.fonrouge.base.types.*
import com.microsoft.sqlserver.jdbc.SQLServerResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*

/**
 * Represents an abstract database class for managing SQL-based operations.
 * Provides functionality such as decoding result sets into JSON objects,
 * executing SQL queries, and mapping SQL results to Kotlin objects.
 * Supports annotations to define relationships and field properties.
 *
 * @property database The database connection to be used for queries and operations.
 */
@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
abstract class SqlDatabase(
    val database: Database,
) {

    /**
     * A class responsible for managing mappings between Kotlin class fields and SQL result sets.
     * Facilitates the decoding process for one-to-one relationships, compound fields, and renamed fields.
     *
     * @property fields List of KCallable objects representing the fields of a Kotlin class used for mapping.
     * @property stringIntMap Mutable map used for tracking mappings between strings and integer indices,
     * typically corresponding to SQL result set metadata.
     */
    class DecodeMap(
        val fields: List<KCallable<*>>,
        val stringIntMap: MutableMap<String, Int>,
    ) {
        var idIsOptional: Boolean? = null
        var oneToOneFields: List<KCallable<*>> = emptyList()
        var compoundFields: List<KCallable<*>> = emptyList()
        var renamedFields: List<String> = emptyList()
        fun setFieldAttributes(klass: KClass<*>) {
            idIsOptional =
                klass.takeIf { it.isSubclassOf(BaseDoc::class) }?.primaryConstructor?.parameters?.firstOrNull { it.name == "_id" }?.isOptional
            oneToOneFields = fields.mapNotNull {
                if (it.hasAnnotation<SqlOneToOne>()) it else null
            }
            compoundFields = fields.mapNotNull {
                if (it.findAnnotation<SqlField>()?.compound == true) it else null
            }
            renamedFields = fields.mapNotNull {
                val sqlField = it.findAnnotation<SqlField>()
                if (sqlField?.name?.isNotEmpty() == true) sqlField.name.uppercase() else null
            }
        }
    }

    /**
     * A mutable map that associates Kotlin classes (`KClass<*>`) with their corresponding `DecodeMap` objects.
     *
     * This map is utilized to efficiently manage and retrieve decoding mappings for different class types
     * during the data transformation process from SQL result sets to Kotlin objects. Each entry in this map
     * ensures that a specific class has a predefined mapping strategy (`DecodeMap`) for fields, enabling
     * optimized data conversions.
     */
    private val mutableMap = mutableMapOf<KClass<*>, DecodeMap>()

    /**
     * A set of classes representing different types of identifiers.
     *
     * This collection includes classes that are commonly used as unique identifiers
     * within the application, such as OId, IntId, LongId, and StringId.
     */
    private val ID_CLASSES = setOf<KClass<out IBaseId<*>>>(OId::class, IntId::class, LongId::class, StringId::class)

    /**
     * Builds a JSON object representation from a given ResultSet based on the provided class type.
     *
     * The method maps the columns in the ResultSet to the corresponding fields in the specified class,
     * leveraging metadata and annotations to construct hierarchical and nested JSON structures as needed.
     *
     * @param klass The KClass representing the type to which the ResultSet should be mapped. This type's 
     *              metadata and annotations will be used to determine how data is structured in the JSON object.
     * @param resultSet The ResultSet containing database query results to be transformed into JSON.
     * @return A JsonObject constructed based on the given ResultSet and class type, containing the
     *         mapped data.
     */
    fun buildJsonFromResultSet(klass: KClass<*>, resultSet: ResultSet): JsonObject {
        val metaData = resultSet.metaData
        val decodeMap: DecodeMap = getDecodeMap(klass, metaData)
        var addedBaseDocPrimaryKeyField = false
        return buildJsonObject {
            for (index in 1..metaData.columnCount) {
                decodeMap.stringIntMap[metaData.getColumnName(index).uppercase()]?.let { indexMap ->
                    val field = decodeMap.fields[indexMap]
                    try {
                        getElementFromClassifier(
                            field = field,
                            resultSet = resultSet,
                            index = index,
                            jsonObjectBuilder = this@buildJsonObject
                        )
                    } catch (e: Exception) {
                        System.err.println("Error on fieldName '${field.name}': ${e.message}")
                        e.printStackTrace()
                    }
                    if (field.name == BaseDoc<*>::_id.name) {
                        addedBaseDocPrimaryKeyField = true
                    }
                }
            }
            decodeMap.oneToOneFields.forEach { field ->
                field.findAnnotation<SqlOneToOne>()?.let {
                    (field.returnType.classifier as? KClass<*>)?.let {
                        put(field.name, buildJsonFromResultSet(it, resultSet))
                    }
                }
            }
            decodeMap.compoundFields.forEach { field ->
                (field.returnType.classifier as? KClass<*>)?.let {
                    put(field.name, buildJsonFromResultSet(it, resultSet))
                }
            }
            if (klass.isSubclassOf(BaseDoc::class) && !addedBaseDocPrimaryKeyField) {
                val kProperty1 = klass.memberProperties.find {
                    it.name == BaseDoc<*>::_id.name && (it.hasAnnotation<SqlIgnoreField>().not())
                }
                (kProperty1?.returnType?.classifier as? KClass<*>)?.let {
                    if (!it.isSubclassOf(Comparable::class)) {
                        put(BaseDoc<*>::_id.name, buildJsonFromResultSet(it, resultSet))
                    }
                }
            }
        }
    }

    /**
     * Executes a SQL query and attempts to find a single item of the specified type [T].
     *
     * @param sql The SQL query string to be executed.
     * @param args A collection of column type/value pairs to provide as arguments for the query. Default is an empty list.
     * @param explicitStatementType An optional explicit statement type for the query execution. Default is null.
     * @return The single result of the query cast to the specified type [T], or null if no result is found or an error occurs.
     */
    suspend inline fun <reified T : Any> findItem(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): T? = newSuspendedTransaction(db = database) {
        exec(stmt = sql, args = args, explicitStatementType = explicitStatementType) { resultSet ->
            if (resultSet.next()) {
                if (T::class.isSubclassOf(Comparable::class)) {
                    getElementFromClassifier(
                        kClass = T::class,
                        resultSet = resultSet,
                        index = 1
                    ) as? T
                } else {
                    sqlEntityTo<T>(resultSet)
                }
            } else null
        }
    }

    /**
     * Executes a SQL query and applies a specified block of code to each row in the result set.
     *
     * @param sql The SQL query string to be executed.
     * @param args A collection of arguments to bind to the SQL query, where each argument is a pair
     *             consisting of an `IColumnType` and a value. Defaults to an empty list if no arguments are provided.
     * @param explicitStatementType The specific type of SQL statement being executed. This is optional
     *                               and can be null if not explicitly specified.
     * @param doBlock The block of code to execute for each row in the result set. This block receives
     *                an instance of `ResultSet` and operates within a `SqlDatabase` context.
     */
    suspend fun forEachResult(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
        doBlock: SqlDatabase.(ResultSet) -> Unit
    ) = newSuspendedTransaction(context = Dispatchers.IO, db = database) {
        exec(sql, args, explicitStatementType) { resultSet ->
            while (resultSet.next()) {
                doBlock(resultSet)
            }
        }
    }

    /**
     * Executes the given SQL query and maps the result to a list of objects of type [T].
     *
     * @param sql The SQL query string to execute.
     * @param args A collection of argument pairs containing their types ([IColumnType]) and values. Defaults to an empty list.
     * @param explicitStatementType The optional explicit type of SQL statement to execute. Can be null if not specified.
     * @return A list of objects of type [T] mapped from the query results, or null if an exception occurs.
     */
    suspend inline fun <reified T> findList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): List<T> = newSuspendedTransaction(context = Dispatchers.IO, db = database) {
        buildList {
            exec(sql, args, explicitStatementType) { resultSet ->
                while (resultSet.next()) {
                    add(sqlEntityTo<T>(resultSet))
                }
            }
        }
    }

    /**
     * Executes the given SQL query and maps the resulting rows to a list of JSON objects.
     *
     * @param sql The SQL query to be executed. Must be a valid SQL string.
     * @param args The parameters to be bound to the query, represented as a list of pairs of column types and their values. Defaults to an empty list.
     * @param explicitStatementType Optional parameter to specify the type of SQL statement being executed. Defaults to null.
     * @return A list of JSON objects representing the query result, where each row is converted to a JSON object.
     */
    suspend inline fun <reified T> findJsonList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): List<JsonObject> = newSuspendedTransaction(context = Dispatchers.IO, db = database) {
        buildList {
            exec(sql, args, explicitStatementType) { resultSet ->
                while (resultSet.next()) {
                    add(sqlEntityToJson<T>(resultSet))
                }
            }
        }
    }

    /**
     * Generates or retrieves a `DecodeMap` for the given class type and result set metadata.
     *
     * This method constructs a mapping between SQL result set column names and the properties
     * of the specified class, enabling efficient data decoding during database operations.
     *
     * @param klass The Kotlin class (`KClass`) for which the decode map is to be generated.
     * @param metaData The metadata (`ResultSetMetaData`) of the SQL result set containing
     *                 information about the columns to be mapped to class properties.
     * @return A `DecodeMap` that contains mappings between the SQL column names and the
     *         indices of corresponding class properties.
     */
    private fun getDecodeMap(klass: KClass<*>, metaData: ResultSetMetaData): DecodeMap = mutableMap.getOrPut(klass) {
        val decodeMap = DecodeMap(klass.memberProperties.toList(), mutableMapOf())
        decodeMap.setFieldAttributes(klass)
        for (i in 1..metaData.columnCount) {
            val sqlName = metaData.getColumnName(i).uppercase()
            if (sqlName !in decodeMap.stringIntMap) {
                val index = findMatchingFieldIndex(decodeMap, sqlName)
                if (index >= 0) {
                    decodeMap.stringIntMap[sqlName] = index
                }
            }
        }
        decodeMap
    }

    /**
     * Finds the index of a field in the decode map that matches the given SQL field name.
     *
     * The method iterates over the fields in the decode map to identify a field that either:
     * - Has a `@SqlField` annotation with a `name` property matching the provided SQL name.
     * - Has a name that matches the provided SQL name (ignoring case), provided that it is not in the list of renamed fields.
     *
     * Fields annotated with `@SqlIgnoreField` are ignored in the matching process.
     *
     * @param decodeMap The decode map containing the fields and associated metadata.
     * @param sqlName The SQL field name to match against the decode map's fields.
     * @return The index of the matching field, or -1 if no match is found.
     */
    private fun findMatchingFieldIndex(decodeMap: DecodeMap, sqlName: String): Int {
        return decodeMap.fields.indexOfFirst { field ->
            if (field.hasAnnotation<SqlIgnoreField>()) return@indexOfFirst false

            val renamedTo = field.findAnnotation<SqlField>()?.name?.ifEmpty { null }
            val matchesAnnotation = renamedTo?.equals(sqlName, ignoreCase = true) == true
            val matchesName = field.name.uppercase() !in decodeMap.renamedFields &&
                    field.name.equals(sqlName, ignoreCase = true)

            matchesAnnotation || matchesName
        }
    }

    /**
     * Retrieves an element from a database `ResultSet` based on the provided classifier.
     * The method interprets the type of element from the classifier to extract the appropriate value.
     * Optionally, the result can also be added into a `JsonObjectBuilder` if provided.
     *
     * @param field Optional KCallable representing the property metadata to process. Can be null.
     * @param kClass Optional KClass representing the classifier used to identify the return type. Defaults to the return type classifier of the given field.
     * @param resultSet An instance of `ResultSet` from which the value will be fetched.
     * @param index The column index within the `ResultSet` to fetch the value.
     * @param jsonObjectBuilder Optional `JsonObjectBuilder` to which the extracted value will be added, using the field's name as the key.
     * @return Extracted value of the type corresponding to the classifier, or null if the value cannot be determined or is null in the database.
     */
    fun getElementFromClassifier(
        field: KCallable<*>? = null,
        kClass: KClass<*>? = field?.returnType?.classifier as? KClass<*>,
        resultSet: ResultSet,
        index: Int,
        jsonObjectBuilder: JsonObjectBuilder? = null,
    ): Any? {
        if (resultSet.getObject(index) == null) {
            field?.let { kCallable ->
                if (kCallable.returnType.isMarkedNullable) {
                    kCallable.name.let { fieldName -> jsonObjectBuilder?.put(fieldName, null) }
                }
            }
            return null
        }
        return when (kClass) {
            String::class, StringId::class -> {
                val result = resultSet.getString(index)
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                result
            }

            Integer::class, IntId::class -> {
                val result: Int = resultSet.getInt(index)
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                result
            }

            Long::class, LongId::class -> {
                val result: Long = resultSet.getLong(index)
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                result
            }

            LocalDateTime::class, OffsetDateTime::class -> when (resultSet) {
                is SQLServerResultSet -> {
                    val result = resultSet.getDateTime(index)?.toLocalDateTime()?.format(
                        DateTimeFormatter.ofPattern(KV_DEFAULT_DATETIME_FORMAT)
                    )
                    field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                    result
                }

                else -> {
                    val result = resultSet.getString(index)
                    field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                    result
                }
            }

            Double::class -> {
                val result = resultSet.getDouble(index)
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                result
            }

            else -> {
                if (kClass?.isSubclassOf(Enum::class) == true) {
                    val result = resultSet.getString(index)
                    field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                    result
                } else {
                    field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, null) }
                    null
                }
            }
        }
    }

    /**
     * Inserts the specified value into the database table.
     *
     * @param T The type of the item to be inserted, constrained to non-nullable types.
     * @param item The object to insert into the database table. Each property of the object
     * must correspond to a column in the specified table.
     * @param tableName The name of the database table where the object will be inserted.
     * @return A [SimpleState] object indicating the result of the operation. The `isOk` property
     * will be true if the insertion was successful, and false otherwise, with an optional error message.
     */
    suspend inline fun <reified T : Any> insertValue(item: T, tableName: String): SimpleState {
        lateinit var simpleState: SimpleState
        newSuspendedTransaction(context = Dispatchers.IO, db = database) {
            val names = mutableListOf<String>()
            val values = mutableListOf<Any>()
            item::class.memberProperties.forEach { kCallable ->
                kCallable.call(item)?.let {
                    val name = kCallable.findAnnotation<SqlField>()?.name
                    names += name ?: kCallable.name
                    values += when (it) {
                        is String -> "'$it'"
                        is OffsetDateTime -> "'${
                            it.toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern(KV_DEFAULT_DATETIME_FORMAT))
                        }'"

                        else -> it.toString()
                    }
                }
            }
            val namesAsString = names.joinToString()
            val valuesAsString = values.joinToString()
            simpleState = try {
                exec(
                    @Suppress("SqlNoDataSourceInspection")
                    "INSERT INTO $tableName ($namesAsString) VALUES ($valuesAsString)"
                )
                SimpleState(isOk = true)
            } catch (e: Exception) {
                SimpleState(isOk = false, msgError = e.message)
            }
        }
        return simpleState
    }

    /**
     * Converts a SQL `ResultSet` into a `JsonObject` representation by mapping the result to the structure
     * defined by the specified generic type `T`.
     *
     * @param T The type representing the structure of the data to be mapped from the `ResultSet`.
     *          This must be a reified type.
     * @param resultSet The SQL `ResultSet` containing the data to be transformed into a JSON object.
     *                  The method processes the result set to build the corresponding JSON structure.
     * @return A `JsonObject` containing the mapped data from the `ResultSet`, structured according to the
     *         generic type `T`.
     */
    inline fun <reified T> sqlEntityToJson(resultSet: ResultSet): JsonObject {
        return buildJsonFromResultSet(T::class, resultSet)
    }

    /**
     * Converts a SQL ResultSet into an entity of the specified type.
     *
     * @param T The type of the entity to which the ResultSet should be converted.
     * @param resultSet The ResultSet containing the SQL query result to be converted.
     * @return An instance of the specified type created by mapping the ResultSet data.
     */
    inline fun <reified T> sqlEntityTo(resultSet: ResultSet): T {
        return Json.decodeFromJsonElement(sqlEntityToJson<T>(resultSet))
    }

    /**
     * Executes a database transaction within a coroutine context and returns the result of the transaction.
     *
     * @param trans A lambda receiver function applied to a Transaction object. This defines the operations
     *              to be performed within the transaction.
     * @return The result of the transaction block with the specified return type.
     */
    suspend fun <T> transaction(trans: Transaction.() -> T): T {
        return newSuspendedTransaction(db = database, statement = trans)
    }
}
