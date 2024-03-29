package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

// TODO: integrate with IApiService
abstract class ICommon<FILT : IApiFilter>(
    var label: String = "",
    open val apiFilterSerializer: KSerializer<FILT>,
) {
    val name: String get() = this::class.simpleName?.removePrefix("Common") ?: "?"

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
