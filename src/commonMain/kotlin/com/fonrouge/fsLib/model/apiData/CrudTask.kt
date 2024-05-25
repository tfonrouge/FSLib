package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable

@Serializable
enum class CrudTask {
    Create,
    Read,
    Update,
    Delete
}

@Suppress("unused")
val CrudTask.isUpsertDelete: Boolean get() = this in listOf(CrudTask.Create, CrudTask.Update, CrudTask.Delete)
