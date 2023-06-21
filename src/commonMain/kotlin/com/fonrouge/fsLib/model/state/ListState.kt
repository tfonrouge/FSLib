package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
data class ListState<T>(
    val data: List<T> = listOf(),
    val last_page: Int = 0,
    val last_row: Int? = null,
    var checksum: String? = null,
    val state: String? = null
) {
    @Suppress("unused")
    fun calcChecksum(): ListState<T> {
        checksum = data.hashCode().toString()
//        println("CHECKSUM: $checksum")
        return this
    }
}
