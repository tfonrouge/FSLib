package com.fonrouge.fsLib.model

@kotlinx.serialization.Serializable
data class ItemContainer<T>(
    val item: T?
)
