package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemContainer<T>(
    var item: T? = null,
    var result: Boolean = item != null,
    var itemAlreadyOn: Boolean = false,
    var description: String? = null,
)
