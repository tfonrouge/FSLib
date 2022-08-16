package com.fonrouge.fsLib.sqlDb

import com.fonrouge.fsLib.annotations.SqlField
import com.microsoft.sqlserver.jdbc.SQLServerResultSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
abstract class SqlDbSettings(
    val sqlDb: Database
) {

    class DecodePair(
        val fields: Array<KCallable<*>>,
        val stringIntMap: MutableMap<String, Int>
    )

    private val mutableMap = mutableMapOf<KClass<*>, DecodePair>()

    fun getDecodeMap(klass: KClass<*>, metaData: ResultSetMetaData): DecodePair {
        val decodePair = mutableMap[klass] ?: DecodePair(klass.memberProperties.toTypedArray(), mutableMapOf()).also {
            mutableMap[klass] = it
        }
        for (i in 1..metaData.columnCount) {
            val sqlName = metaData.getColumnName(i).uppercase()
            if (!decodePair.stringIntMap.containsKey(sqlName)) {
                val index = decodePair.fields.indexOfFirst { field ->
                    val name = field.findAnnotation<SqlField>()?.name ?: field.name
                    name.equals(other = sqlName, ignoreCase = true)
                }
                if (index >= 0) {
                    decodePair.stringIntMap[sqlName] = index
                }
            }
        }
        return decodePair
    }

    inline fun <reified T> sqlEntityTo(resultSet: ResultSet): T {
        val metaData = resultSet.metaData
        val decodePair = getDecodeMap(T::class, metaData)
        val jsonObject = buildJsonObject {
            for (i in 1..metaData.columnCount) {
                decodePair.stringIntMap[metaData.getColumnName(i).uppercase()]?.let {
                    val field = decodePair.fields[it]
                    when (resultSet) {
                        is SQLServerResultSet -> sqlServerResultSet(field, resultSet, i)
                        else -> simpleResultSet(field, resultSet, i)
                    }
                }
            }
        }
        return Json.decodeFromJsonElement(jsonObject)
    }

    inline fun <reified T> sqlEntityToList(resultSet: ResultSet): List<T> {
        val result = mutableListOf<T>()
        while (resultSet.next()) {
            result.add(sqlEntityTo(resultSet))
        }
        return result
    }

    fun JsonObjectBuilder.sqlServerResultSet(field: KCallable<*>, resultSet: SQLServerResultSet, i: Int) {
        when (field.returnType.classifier) {
            String::class -> put(field.name, resultSet.getString(i))
            Integer::class -> put(field.name, resultSet.getInt(i))
            LocalDateTime::class -> put(
                field.name,
                resultSet.getDateTime(i)?.toLocalDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            Double::class -> put(field.name, resultSet.getDouble(i))
            else -> put(field.name, null)
        }
    }

    fun JsonObjectBuilder.simpleResultSet(field: KCallable<*>, resultSet: ResultSet, i: Int) {
        when (field.returnType.classifier) {
            String::class -> put(field.name, resultSet.getString(i))
            Integer::class -> put(field.name, resultSet.getInt(i))
            LocalDateTime::class -> put(
                field.name,
                resultSet.getString(i)
            )

            Double::class -> put(field.name, resultSet.getDouble(i))
            else -> put(field.name, null)
        }
    }
}
