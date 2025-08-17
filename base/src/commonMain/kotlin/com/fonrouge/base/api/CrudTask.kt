package com.fonrouge.base.api

import com.fonrouge.base.enums.XEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum class that defines the possible CRUD operations in an application.
 *
 * @param encoded A string representation of the CRUD operation.
 */
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

/**
 * Indicates whether the CRUD task is either Create, Update, or Delete.
 */
@Suppress("unused")
val CrudTask.isUpsertDelete: Boolean
    get() = this in listOf(
        CrudTask.Create,
        CrudTask.Update,
        CrudTask.Delete
    )
