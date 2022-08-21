package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.KPair
import kotlinx.serialization.Contextual

@kotlinx.serialization.Serializable
data class ItemContainer<T>(
    var item: T? = null,
    var result: Boolean = item != null,
    var description: String? = null,
    var onCreateDefaultValue: List<KPair<T, @Contextual Any>>? = null,
)
