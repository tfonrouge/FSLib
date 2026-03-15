package com.fonrouge.base.common

import com.fonrouge.base.api.IApiFilter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Abstract class representing a common interface for handling filters and labels.
 *
 * The [apiFilterSerializer] is derived automatically from [filterKClass] via `KClass.serializer()`.
 *
 * @param FILT The type of the filter extending IApiFilter.
 * @property label A string label associated with this instance.
 * @param filterKClass KClass for the filter type, used to derive [apiFilterSerializer] automatically.
 */
@OptIn(InternalSerializationApi::class)
abstract class ICommon<FILT : IApiFilter<*>>(
    var label: String = "",
    filterKClass: KClass<FILT>,
) {
    /**
     * Serializer for the filter type, derived from [filterKClass].
     */
    open val apiFilterSerializer: KSerializer<FILT> = filterKClass.serializer()

    /**
     * Logical name for this container, used as a fallback for URL generation.
     * Defaults to the class simple name with the "Common" prefix stripped.
     * Subclasses (including anonymous objects from [simpleContainer]) may override.
     */
    open val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"

    /**
     * Creates a default instance of the filter by deserializing an empty JSON object.
     *
     * @return A new instance of [FILT].
     * @throws Exception if the filter class has required constructor parameters.
     */
    open fun apiFilterInstance(): FILT {
        return try {
            Json.decodeFromString(apiFilterSerializer, "{}")
        } catch (e: SerializationException) {
            val errMsg = """
                Error creating instance of apiFilter: ${e.message},
                hint: [${apiFilterSerializer.descriptor} ::class must *not* have required constructor parameters,
                """.trimIndent()
            e.printStackTrace()
            throw Exception(errMsg)
        }
    }
}
