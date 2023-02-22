package com.fonrouge.fsLib.sqlDb

import com.fonrouge.fsLib.annotations.SqlField
import com.fonrouge.fsLib.annotations.SqlOneToOne
import com.fonrouge.fsLib.serializers.KV_DEFAULT_DATETIME_FORMAT
import com.microsoft.sqlserver.jdbc.SQLServerResultSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.transaction
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

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
abstract class SqlDbSettings(
    val sqlDb: Database
) {

    class DecodeMap(
        val fields: Array<KCallable<*>>,
        val stringIntMap: MutableMap<String, Int>,

        ) {
        val oneToOneFields: Array<KCallable<*>> =
            fields.mapNotNull {
                if (it.hasAnnotation<SqlOneToOne>()) it else null
            }.toTypedArray()
    }

    private val mutableMap = mutableMapOf<KClass<*>, DecodeMap>()

    inline fun <reified T : Any> findItem(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): T? {
        var result: T? = null
        transaction(db = sqlDb) {
            try {
                exec(sql, args, explicitStatementType) { resultSet ->
                    if (resultSet.next()) {
                        result = if (T::class.isSubclassOf(Comparable::class)) {
                            getElementFromClasifier(
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

    inline fun <reified T> findList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): List<T> {
        val result = mutableListOf<T>()
        transaction(db = sqlDb) {
            try {
                exec(sql, args, explicitStatementType) { resultSet ->
                    while (resultSet.next()) {
                        try {
                            result.add(sqlEntityTo(resultSet))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: ExposedSQLException) {
                System.err.println("SQL findList() error: ${e.message} on SQL string: $sql")
                e.printStackTrace()
            }
        }
        return result
    }

    private fun getDecodeMap(klass: KClass<*>, metaData: ResultSetMetaData): DecodeMap {
        val decodeMap = mutableMap[klass] ?: DecodeMap(klass.memberProperties.toTypedArray(), mutableMapOf()).also {
            mutableMap[klass] = it
        }
        for (i in 1..metaData.columnCount) {
            val sqlName = metaData.getColumnName(i).uppercase()
            if (!decodeMap.stringIntMap.containsKey(sqlName)) {
                val index = decodeMap.fields.indexOfFirst { field ->
                    val name = field.findAnnotation<SqlField>()?.name ?: field.name
                    name.equals(other = sqlName, ignoreCase = true)
                }
                if (index >= 0) {
                    decodeMap.stringIntMap[sqlName] = index
                }
            }
        }
        return decodeMap
    }

    fun getElementFromClasifier(
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
            String::class -> {
                val result = resultSet.getString(index)
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, result) }
                result
            }

            Integer::class -> {
                val result: Int = resultSet.getInt(index)
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
                field?.name?.let { fieldName -> jsonObjectBuilder?.put(fieldName, null) }
                null
            }
        }
    }

    fun buildJsonFromResultSet(klass: KClass<*>, resultSet: ResultSet): JsonObject {
        val metaData = resultSet.metaData
        val decodeMap = getDecodeMap(klass, metaData)
        return buildJsonObject {
            for (index in 1..metaData.columnCount) {
                decodeMap.stringIntMap[metaData.getColumnName(index).uppercase()]?.let { indexMap ->
                    val field = decodeMap.fields[indexMap]
                    getElementFromClasifier(
                        field = field,
                        resultSet = resultSet,
                        index = index,
                        jsonObjectBuilder = this@buildJsonObject
                    )
                }
            }
            decodeMap.oneToOneFields.forEach { field ->
                field.findAnnotation<SqlOneToOne>()?.let {
                    (field.returnType.classifier as? KClass<*>)?.let {
                        put(field.name, buildJsonFromResultSet(it, resultSet))
                    }
                }
            }
        }
    }

    inline fun <reified T> sqlEntityTo(resultSet: ResultSet): T {
        val jsonObject = buildJsonFromResultSet(T::class, resultSet)
        return Json.decodeFromJsonElement(jsonObject)
    }

    fun <T> transaction(trans: Transaction.() -> T): T {
        return transaction(db = sqlDb, trans)
    }
}
