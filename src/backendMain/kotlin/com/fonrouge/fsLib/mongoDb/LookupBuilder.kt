package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.mongoDb.CTableDb.Companion.map1
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class LookupBuilder<T : BaseModel<*>, U : BaseModel<W>, V, W>(
    private val cTableDb: KClass<out CTableDb<U, W>>,
    private val fieldToField: FieldToField<T, U, V>,
    val resultProperty: KProperty1<T, U?>,
    private val matchFilters: List<Bson>? = null
) {

    internal fun addToPipeline(pipeline: MutableList<Bson>, modelLookup: ModelLookup<*, *>) {
        val match = mutableListOf(
            expr(
                eq from listOf(
                    fieldToField.foreign,
                    "$\$letVar1"
                )
            )
        )

        matchFilters?.let { match.addAll(it) }

        val pip2 = mutableListOf(
            match(*match.toTypedArray())
        )

        map1[cTableDb]?.buildLookup(*modelLookup.modelLookup)?.let {
            pip2 += it
        }

        pipeline += lookup(
            from = map1[cTableDb]?.mongoColl?.namespace?.collectionName ?: "?",
            let = listOf(Variable("letVar1", fieldToField.local)),
            resultProperty = resultProperty,
            *pip2.toTypedArray()
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }

    class FieldToField<T, U, V>(
        val local: KProperty1<T, V>,
        val foreign: KProperty1<U, V>
    )
}

@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, String?>.localToForeign(that: KProperty1<U, String?>) =
    LookupBuilder.FieldToField<T, U, String?>(this, that)

@JvmName("localToForeignTInt?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, Int?>.localToForeign(that: KProperty1<U, Int?>) =
    LookupBuilder.FieldToField<T, U, Int?>(this, that)

@JvmName("localToForeignTLong?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, Long?>.localToForeign(that: KProperty1<U, Long?>) =
    LookupBuilder.FieldToField<T, U, Long?>(this, that)

@JvmName("localToForeignTLocalDateTime?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, LocalDateTime?>.localToForeign(that: KProperty1<U, LocalDateTime?>) =
    LookupBuilder.FieldToField<T, U, LocalDateTime?>(this, that)

