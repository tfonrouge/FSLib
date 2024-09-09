package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class ApiFilter(
    override var masterItemId: Unit? = null,
) : IApiFilter<Unit>()
