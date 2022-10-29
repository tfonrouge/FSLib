package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class ListContainer<T>(
    val data: List<T> = listOf(),
    val last_page: Int = 0,
    val last_row: Int? = null,
    val checksum: String? = null,
    val state: String? = null
)
