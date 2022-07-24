package com.fonrouge.fsLib.model

@kotlinx.serialization.Serializable
data class ItemContainer<T>(
    val item: T? = null,
    val result: Boolean = item != null,
    val description: String? = null
)
