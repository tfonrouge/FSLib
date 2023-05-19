package com.fonrouge.fsLib.sqlDb

import com.fonrouge.fsLib.annotations.SqlField
import com.fonrouge.fsLib.annotations.SqlIgnoreField
import com.fonrouge.fsLib.annotations.SqlOneToOne
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.KV_DEFAULT_DATETIME_FORMAT
import com.fonrouge.fsLib.serializers.StringId
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

@OptIn(ExperimentalSerializationApi::class)
@Suppress("unused")
abstract class SqlDatabase(
    val database: Database
) {

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

    private val mutableMap = mutableMapOf<KClass<*>, DecodeMap>()

    suspend inline fun <reified T : Any> findItem(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): T? {
        var result: T? = null
        newSuspendedTransaction(db = database) {
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

    suspend inline fun <reified T> forEachResult(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
        /* TODO: how to make this block suspended */ crossinline doBlock: (T) -> Unit,
    ) {
        newSuspendedTransaction(context = Dispatchers.IO, db = database) {
            try {
                exec(sql, args, explicitStatementType) { resultSet ->
                    while (resultSet.next()) {
                        try {
                            doBlock(sqlEntityTo<T>(resultSet))
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
    }

    suspend inline fun <reified T> findList(
        @Language("SQL") sql: String,
        args: Iterable<Pair<IColumnType, Any?>> = emptyList(),
        explicitStatementType: StatementType? = null,
    ): List<T> {
        val result = mutableListOf<T>()
        forEachResult(
            sql = sql,
            doBlock = { t: T -> result.add(t) },
            args = args,
            explicitStatementType = explicitStatementType,
        )
        return result
    }

    private fun getDecodeMap(klass: KClass<*>, metaData: ResultSetMetaData): DecodeMap {
        val decodeMap = mutableMap[klass] ?: DecodeMap(klass.memberProperties.toList(), mutableMapOf()).also {
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
        val decodeMap: DecodeMap = getDecodeMap(klass, metaData)
        var addedBaseDocPrimaryKeyField = false
        return buildJsonObject {
            for (index in 1..metaData.columnCount) {
                decodeMap.stringIntMap[metaData.getColumnName(index).uppercase()]?.let { indexMap ->
                    val field = decodeMap.fields[indexMap]
                    try {
                        getElementFromClasifier(
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

    inline fun <reified T> sqlEntityTo(resultSet: ResultSet): T {
        val jsonObject = buildJsonFromResultSet(T::class, resultSet)
        return Json.decodeFromJsonElement(jsonObject)
    }

    suspend fun <T> transaction(trans: Transaction.() -> T): T {
        return newSuspendedTransaction(db = database, statement = trans)
    }
}
