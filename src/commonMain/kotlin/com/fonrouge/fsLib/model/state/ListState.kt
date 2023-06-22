package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
data class ListState<T : Any>(
    val data: List<T> = listOf(),
    val last_page: Int = 0,
    val last_row: Int? = null,
    var contentHashCode: Int? = null,
    val state: String? = null
) {
    @Suppress("unused")
    fun setContentHashCode(): ListState<T> {
        contentHashCode = (data as List<Any>).toTypedArray().contentDeepHashCode()
        return this
    }
}
