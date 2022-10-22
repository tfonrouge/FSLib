package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class ListContainer<T>(
    val data: List<T> = listOf(),
    val last_page: Int = 0,
    val last_row: Int? = null,
    val contentStatus: ContentStatus = ContentStatus.Active
) {
    enum class ContentStatus {
        Active,
        Ignore
    }
}
