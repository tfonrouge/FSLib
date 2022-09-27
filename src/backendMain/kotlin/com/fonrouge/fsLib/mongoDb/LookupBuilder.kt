package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.mongoDb.CTableDb.Companion.map1
import com.mongodb.client.model.UnwindOptions
import org.bson.conversions.Bson
import org.litote.kmongo.unwind
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class LookupBuilder<T : BaseModel<*>, U : BaseModel<W>, V, W>(
    private val cTableDb: KClass<out CTableDb<U, W>>,
    private val localToForeign: LocalToForeign<T, U, V>,
    val resultProperty: KProperty1<T, U?>,
) {

    internal fun addToPipeline(pipeline: MutableList<Bson>, modelLookup: ModelLookup<*, *>) {
        val pip2 = mutableListOf<Bson>()

        map1[cTableDb]?.buildLookup(*modelLookup.modelLookup)?.let {
            pip2 += it
        }

        pipeline += lookup5(
            from = map1[cTableDb]?.mongoColl?.namespace?.collectionName ?: "?",
            localField = localToForeign.local.name,
            foreignField = localToForeign.foreign.name,
            pipeline = pip2,
            newAs = resultProperty.name,
        )
        pipeline += resultProperty.unwind(UnwindOptions().preserveNullAndEmptyArrays(true))
    }

    class LocalToForeign<T, U, V>(
        val local: KProperty1<T, V>,
        val foreign: KProperty1<U, V>
    )
}

@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, String?>.localToForeign(that: KProperty1<U, String?>) =
    LookupBuilder.LocalToForeign<T, U, String?>(this, that)

@JvmName("localToForeignTInt?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, Int?>.localToForeign(that: KProperty1<U, Int?>) =
    LookupBuilder.LocalToForeign<T, U, Int?>(this, that)

@JvmName("localToForeignTLong?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, Long?>.localToForeign(that: KProperty1<U, Long?>) =
    LookupBuilder.LocalToForeign<T, U, Long?>(this, that)

@JvmName("localToForeignTLocalDateTime?")
@Suppress("unused")
infix fun <T : BaseModel<*>, U : BaseModel<*>> KProperty1<T, LocalDateTime?>.localToForeign(that: KProperty1<U, LocalDateTime?>) =
    LookupBuilder.LocalToForeign<T, U, LocalDateTime?>(this, that)

