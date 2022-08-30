package com.fonrouge.fsLib.sqlDb

import com.fonrouge.fsLib.annotations.SqlField
import com.fonrouge.fsLib.annotations.SqlOneToOne
import com.fonrouge.fsLib.serializers.FSLocalDateTimeSerializer
import com.microsoft.sqlserver.jdbc.SQLServerResultSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
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

    inline fun <reified T> findItem(@Language("SQL") sql: String): T? {
        var result: T? = null
        transaction(db = sqlDb) {
            try {
                exec(sql) { resultSet ->
                    if (resultSet.next()) {
                        result = sqlEntityTo<T>(resultSet)
                    }
                }
            } catch (e: ExposedSQLException) {
                val s = "SQL findItem() error: ${e.message}"
                System.err.println(s)
                println(s)
            }
        }
        return result
    }

    inline fun <reified T> findList(@Language("SQL") sql: String): List<T> {
        val result = mutableListOf<T>()
        transaction(db = sqlDb) {
            try {
                exec(sql) { resultSet ->
                    while (resultSet.next()) {
                        result.add(sqlEntityTo(resultSet))
                    }
                }
            } catch (e: ExposedSQLException) {
                val s = "SQL findList() error: ${e.message}"
                System.err.println(s)
                println(s)
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

    private fun JsonObjectBuilder.putElement(field: KCallable<*>, resultSet: ResultSet, index: Int) {
        when (field.returnType.classifier) {
            String::class -> put(field.name, resultSet.getString(index))
            Integer::class -> put(field.name, resultSet.getInt(index))
            LocalDateTime::class -> when (resultSet) {
                is SQLServerResultSet -> put(
                    field.name,
                    resultSet.getDateTime(index)?.toLocalDateTime()?.format(
                        DateTimeFormatter.ofPattern(FSLocalDateTimeSerializer.KV_DEFAULT_DATETIME_FORMAT)
                    )
                )

                else -> put(field.name, resultSet.getString(index))
            }

            Double::class -> put(field.name, resultSet.getDouble(index))
            else -> put(field.name, null)
        }
    }

    fun buildJsonFromResultSet(klass: KClass<*>, resultSet: ResultSet): JsonObject {
        val metaData = resultSet.metaData
        val decodeMap = getDecodeMap(klass, metaData)
        return buildJsonObject {
            for (i in 1..metaData.columnCount) {
                decodeMap.stringIntMap[metaData.getColumnName(i).uppercase()]?.let {
                    val field = decodeMap.fields[it]
                    putElement(field, resultSet, i)
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
}
