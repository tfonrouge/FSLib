package com.fonrouge.fsLib.model.apiData

import com.fonrouge.fsLib.enums.XEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CrudTask(override val encoded: String) : XEnum {
    @SerialName("C")
    Create("C"),

    @SerialName("R")
    Read("R"),

    @SerialName("U")
    Update("U"),

    @SerialName("D")
    Delete("D")
}

@Suppress("unused")
val CrudTask.isUpsertDelete: Boolean
    get() = this in listOf(
        CrudTask.Create,
        CrudTask.Update,
        CrudTask.Delete
    )
