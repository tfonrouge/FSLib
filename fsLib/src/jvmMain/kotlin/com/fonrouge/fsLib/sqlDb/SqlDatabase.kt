package com.fonrouge.fsLib.sqlDb

import com.fonrouge.fsLib.annotations.SqlField
import com.fonrouge.fsLib.annotations.SqlIgnoreField
import com.fonrouge.fsLib.annotations.SqlOneToOne
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.serializers.KV_DEFAULT_DATETIME_FORMAT
import com.fonrouge.fsLib.types.IntId
import com.fonrouge.fsLib.types.LongId
import com.fonrouge.fsLib.types.StringId
import com.microsoft.sqlserver.jdbc.SQLServerResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.exceptions.ExposedSQLException
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
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

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
    val database: Database
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
        var oneToOneFields: List<KCallable<*>> = emptyList()

        var compoundFields: List<KCallable<*>> = emptyList()

        var renamedFields: List<String> = emptyList()

        fun setFieldListAttributes() {
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
     * Constructs a JSON object by mapping data from the provided `ResultSet` to the specified class type.
     * This method utilizes a decoding map and reflection to match columns in the `ResultSet` to the
     * fields of the given class, while also supporting nested mappings and compound fields.
     *
     * @param klass The KClass object representing the type to which the `ResultSet` is to be mapped.
     *              It determines the structure of the resulting JSON object.
     * @param resultSet The `ResultSet` containing the data to be transformed into a JSON object.
     *                  The method iterates over its metadata to identify and map fields.
     * @return A `JsonObject` representing the data from the `ResultSet`, mapped according to the
     *         structure of the specified class.
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
     * Executes the provided SQL query and returns a single result of type `T`.
     *
     * This method performs a suspended database query execution and maps the first result row
     * to the specified Kotlin class type `T`. If no result is found, it returns `null`.
     *
     * @param sql The SQL query string to be executed.
     * @param args A collection of pairs representing the column types and their corresponding values
     *             to be used as arguments in the query. Defaults to an empty list.
     * @param explicitStatementType An optional `StatementType` used to explicitly define
     *                              the type of SQL statement being executed. Defaults to `null`.
     * @return A nullable result of type `T`, representing the first row of the query result,
     *         or `null` if no result is found.
     */
    suspend inline fun <reified T : Any> findItem(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): T? {
        var result: T? = null
        newSuspendedTransaction(db = database) {
            try {
                exec(sql, args, explicitStatementType) { resultSet ->
                    if (resultSet.next()) {
                        result = if (T::class.isSubclassOf(Comparable::class)) {
                            getElementFromClassifier(
                                kClass = T::class,
                                resultSet = resultSet,
                                index = 1
                            ) as? T
                        } else {
                            sqlEntityTo<T>(resultSet)
                        }
                    }
                }
            } catch (e: ExposedSQLException) {
                val s = "SQL findItem() error: ${e.message} on SQL string: $sql"
                System.err.println(s)
                println(s)
            }
        }
        return result
    }

    /**
     * Executes a provided SQL query and applies a specified block of logic to each resulting row,
     * collecting the results into a list of a specified type.
     *
     * @param T The type to which rows in the result set should be transformed.
     * @param sql The SQL query string to be executed.
     * @param args A collection of pairs representing the column types and their corresponding values
     *             to be used as arguments in the query. Defaults to an empty list.
     * @param explicitStatementType An optional `StatementType` used to explicitly define the type of
     *                              SQL statement being executed. Defaults to null.
     * @param debug A flag to enable debug logging, printing the SQL statement being executed. Defaults to false.
     * @param doBlock A lambda function that defines the logic for transforming a single `ResultSet` row
     *                into an object of type `T`. If null is returned from this block, no item is added to the list.
     * @return A list of objects of type `T`, representing the transformed rows of the result set.
     */
    suspend inline fun <reified T> forEachResult(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
        debug: Boolean = false,
        /* TODO: how to make this block suspended */
        crossinline doBlock: (ResultSet) -> T? = { resultSet ->
            sqlEntityTo<T>(resultSet)
        },
    ): List<T> {
        if (debug) {
            println("SQL CMD ${T::class.simpleName}\n$sql")
        }
        val result = mutableListOf<T>()
        return newSuspendedTransaction(context = Dispatchers.IO, db = database) {
            try {
                exec(sql, args, explicitStatementType) { resultSet ->
                    while (resultSet.next()) {
                        try {
                            doBlock(resultSet)?.let { result.add(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                result
            } catch (e: ExposedSQLException) {
                System.err.println("SQL findList() error: ${e.message} on SQL string: $sql")
                e.printStackTrace()
                result
            }
        }
    }

    /**
     * Executes an SQL query and transforms the resulting rows into a list of type `T`.
     *
     * @param T The type to which rows in the result set will be transformed.
     * @param sql The SQL query string to be executed.
     * @param args A collection of pairs representing the column types and their corresponding values
     *             to be used as arguments in the query. Defaults to an empty list.
     * @param explicitStatementType An optional `StatementType` used to explicitly define the type of SQL
     *                              statement being executed. Defaults to `null`.
     * @param debug A flag to enable debug logging, printing the SQL statement being executed. Defaults to `false`.
     * @param doBlock A lambda function that specifies the logic for converting a single `ResultSet` row
     *                into an object of type `T`. If the block returns `null`, no item is added to the list.
     * @return A list of objects of type `T`, representing the transformed rows of the result set.
     */
    suspend inline fun <reified T> findList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
        debug: Boolean = false,
        crossinline doBlock: (ResultSet) -> T? = { resultSet ->
            sqlEntityTo<T>(resultSet)
        }
    ): List<T> {
        return forEachResult<T>(
            sql = sql,
            doBlock = doBlock,
            args = args,
            explicitStatementType = explicitStatementType,
            debug = debug,
        )
    }

    /**
     * Executes the provided SQL query and maps the resulting rows to a list of `JsonObject`s.
     *
     * @param T The type representing the structure of data mapped from the SQL result set.
     * @param sql The SQL query string to be executed.
     * @param args A collection of pairs representing the column types and their corresponding values
     *             to be used as arguments in the query. Defaults to an empty list.
     * @param explicitStatementType An optional `StatementType` used to explicitly define the type of
     *                              SQL statement being executed. Defaults to null.
     * @param debug A flag to enable debug logging which prints the SQL query being executed. Defaults to false.
     * @return A list of `JsonObject`s representing the data retrieved and transformed according to the
     *         specified type structure.
     */
    suspend inline fun <reified T> findJsonList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
        debug: Boolean = false,
    ): List<JsonObject> {
        return forEachResult<JsonObject>(
            sql = sql,
            doBlock = { resultSet ->
                sqlEntityToJson<T>(resultSet)
            },
            args = args,
            explicitStatementType = explicitStatementType,
            debug = debug,
        )
    }

    /**
     * Generates or retrieves a `DecodeMap` object for the given class and `ResultSetMetaData`.
     * This map is used to match SQL column names to the corresponding class fields.
     *
     * @param klass The `KClass` of the target type for which decoding is being performed.
     * @param metaData The `ResultSetMetaData` containing details about the columns in a SQL query result set.
     * @return A `DecodeMap` object that maps SQL column names to indices of the corresponding fields
     *         in the given class, enabling efficient data decoding.
     */
    private fun getDecodeMap(klass: KClass<*>, metaData: ResultSetMetaData): DecodeMap {
        val decodeMap =
            mutableMap[klass] ?: DecodeMap(klass.memberProperties.toList(), mutableMapOf()).also {
                mutableMap[klass] = it
                it.setFieldListAttributes()
            }
        for (i in 1..metaData.columnCount) {
            val sqlName = metaData.getColumnName(i).uppercase()
            if (!decodeMap.stringIntMap.containsKey(sqlName)) {
                val index = decodeMap.fields.indexOfFirst { field ->
                    val renamedTo = field.findAnnotation<SqlField>()?.name?.ifEmpty { null }
                    val sqlIgnoreField = field.hasAnnotation<SqlIgnoreField>()
                    !sqlIgnoreField && ((renamedTo?.equals(
                        sqlName,
                        true
                    ) == true) || (field.name.uppercase() !in decodeMap.renamedFields && field.name.equals(
                        sqlName,
                        true
                    )))
                }
                if (index >= 0) {
                    decodeMap.stringIntMap[sqlName] = index
                }
            }
        }
        return decodeMap
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
        jsonObjectBuilder: JsonObjectBuilder? = null
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
