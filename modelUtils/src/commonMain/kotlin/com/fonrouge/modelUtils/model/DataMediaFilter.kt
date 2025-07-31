package com.fonrouge.modelUtils.model

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlinx.serialization.Serializable

@Serializable
data class DataMediaFilter(
    val classifierDoc: String? = null,
    val serializedIdDoc: String? = null,
) : IApiFilter<Unit>()
