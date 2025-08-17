package com.fonrouge.fsUtils.model

import com.fonrouge.base.api.IApiFilter
import kotlinx.serialization.Serializable

@Serializable
data class DataMediaFilter(
    val classifierDoc: String? = null,
    val serializedIdDoc: String? = null,
) : IApiFilter<Unit>()
