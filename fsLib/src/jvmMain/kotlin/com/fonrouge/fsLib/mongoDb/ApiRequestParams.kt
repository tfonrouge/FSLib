package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.PreLookupField
import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.types.IntId
import com.fonrouge.fsLib.types.LongId
import com.fonrouge.fsLib.types.StringId
import dev.kilua.rpc.RemoteFilter
import dev.kilua.rpc.RemoteSorter
import org.bson.*
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/**
 * A data class representing the first stage in a pipeline,
 * used for operations involving paginated data and sorting/matching BSON documents.
 *
 * @property pageSize The number of items per page, can be null if not paginated.
 * @property page The current page number, can be null if not paginated.
 * @property remoteFilters An optional BSON document to match documents after a lookup operation.
 * @property remoteSorters An optional BSON document to sort documents before a lookup operation.
 */
data class ApiRequestParams(
    val pageSize: Int?,
    val page: Int?,
    private val remoteFilters: List<RemoteFilter>? = null,
    private val remoteSorters: List<RemoteSorter>? = null,
) {
    /**
     * Finds the type of a field within a Kotlin class and verifies whether the field is annotated with `@PreLookupField`.
     * It supports nested field names using dot notation to traverse deeper into properties.
     *
     * @param kClass The Kotlin class to search for the field.
     * @param fieldName The name of the field to look for. Supports dot notation for nested properties.
     * @return A pair containing the field's type as a [KClassifier] and a Boolean indicating if the field is annotated with `@PreLookupField`.
     *         Returns null if the field is not found.
     */
    private fun findFieldType(kClass: KClass<*>, fieldName: String): Pair<KClassifier, Boolean>? {
        val properties = kClass.memberProperties
        return if (fieldName.contains('.')) {
            val kproperty1 = properties.firstOrNull { it.name == fieldName.substringBefore('.') }
            val classifier = kproperty1?.returnType?.classifier
            val kClass: KClass<*> = classifier as? KClass<*> ?: return null
            if (kClass.isSubclassOf(Collection::class)) {
                // TODO: solve collection element type, here String class is hardcoded
                Pair(String::class, kproperty1.hasAnnotation<PreLookupField>())
            } else findFieldType(kClass, fieldName.substringAfter('.'))
        } else {
            properties.firstOrNull { it.name == fieldName }?.let { kProperty ->
                kProperty.returnType.classifier?.let { classifier ->
                    Pair(classifier, kProperty.hasAnnotation<PreLookupField>())
                }
            }
        }
    }

    /**
     * Constructs a set of BSON-based filters for database queries, segregated into pre-main and post-main lookup filters.
     *
     * @param commonContainer An instance of [ICommonContainer] that provides information about the item type and its associated metadata.
     * @return A [MatchLists] object containing the pre-main and post-main lookup filters, or null if no filters are generated.
     */
    fun bsonMatches(commonContainer: ICommonContainer<*, *, *>): MatchLists? {
        val preMainLookup: MutableList<Bson> = mutableListOf()
        val postMainLookup: MutableList<Bson> = mutableListOf()
        remoteFilters?.let {
            remoteFilters.forEach { remoteFilter ->
                val (kClasiffier, isPreLookupField) = findFieldType(
                    kClass = commonContainer.itemKClass,
                    fieldName = remoteFilter.field
                ) ?: return@forEach
                val value: BsonValue? = when (kClasiffier) {
                    Array<String>::class, String::class, StringId::class -> {
                        when (remoteFilter.type) {
                            "like" -> BsonDocument(
                                "\$regex",
                                BsonString(remoteFilter.value)
                            ).append("\$options", BsonString("i"))

                            else -> BsonString(remoteFilter.value)
                        }
                    }

                    Int::class, IntId::class -> remoteFilter.value?.toIntOrNull()
                        ?.let { BsonInt32(it) }

                    Long::class, LongId::class -> remoteFilter.value?.toLongOrNull()
                        ?.let { BsonInt64(it) }

                    Double::class -> remoteFilter.value?.toDoubleOrNull()
                        ?.let { BsonDouble(it) }

                    else -> null
                }
                value?.let {
                    val bsonDocument = BsonDocument(remoteFilter.field, value)
                    if (!isPreLookupField && remoteFilter.field.contains("."))
                        postMainLookup += bsonDocument
                    else
                        preMainLookup += bsonDocument
                }
            }
        }
        return if (preMainLookup.isEmpty() && postMainLookup.isEmpty())
            null
        else MatchLists(
            preMainLookup = if (preMainLookup.isEmpty()) null else preMainLookup,
            postMainLookup = if (postMainLookup.isEmpty()) null else postMainLookup,
        )
    }

    /**
     * Constructs BSON-based sorters for database queries, dividing them into pre-main and post-main lookup sorters.
     * The sorters are created based on the provided `remoteSorters` field, where each sorter determines the field to sort
     * and the direction (`asc` or `desc`). Sorters are split into pre-main and post-main lists depending on whether the
     * field contains a dot notation.
     *
     * @return A [SortLists] object containing the pre-main and post-main lookup sorters, or null if no sorters are generated.
     */
    fun bsonSorters(): SortLists? {
        val preMainLookup = BsonDocument()
        val postMainLookup = BsonDocument()
        remoteSorters?.forEach { remoteSorter ->
            val pair: Pair<String, BsonInt32> = remoteSorter.field to when (remoteSorter.dir) {
                "asc" -> BsonInt32(1)
                "desc" -> BsonInt32(-1)
                else -> BsonInt32(1)
            }
            if (remoteSorter.field.contains("."))
                postMainLookup.append(pair.first, pair.second)
            else
                preMainLookup.append(pair.first, pair.second)
        }
        return if (preMainLookup.isEmpty() && postMainLookup.isEmpty())
            null
        else SortLists(
            preMainLookup = if (preMainLookup.isEmpty()) null else preMainLookup,
            postMainLookup = if (postMainLookup.isEmpty()) null else postMainLookup
        )
    }

    data class MatchLists(
        val preMainLookup: MutableList<Bson>? = null,
        val postMainLookup: MutableList<Bson>? = null,
    )

    data class SortLists(
        val preMainLookup: BsonDocument? = null,
        val postMainLookup: BsonDocument? = null,
    )
}
