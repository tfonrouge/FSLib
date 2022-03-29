package com.fonrouge.fsLib

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class ApiParam(
    var filter: JsonObject? = null,
    var sort: JsonObject? = null
)
