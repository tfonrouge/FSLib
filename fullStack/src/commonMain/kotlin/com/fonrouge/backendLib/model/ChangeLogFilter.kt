package com.fonrouge.backendLib.model

import com.fonrouge.base.api.IApiFilter
import kotlinx.serialization.Serializable

@Serializable
data class ChangeLogFilter(
    val action: IChangeLog.Action? = null,
    val className: String? = null,
    val serializedId: String? = null,
) : IApiFilter<Unit>()