package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable

@Serializable
data class ApiFilter(
    override var masterItemIdSerialized: String? = null,
) : IApiFilter
