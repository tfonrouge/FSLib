package com.fonrouge.fsLib.model

@kotlinx.serialization.Serializable
data class ItemContainer<T>(
    var item: T? = null,
    var result: Boolean = item != null,
    var description: String? = null
)
