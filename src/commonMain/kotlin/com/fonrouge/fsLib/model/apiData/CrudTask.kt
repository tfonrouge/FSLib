package com.fonrouge.fsLib.model.apiData

import kotlinx.serialization.Serializable

@Serializable
enum class CrudTask {
    Create,
    Read,
    Update,
    Delete
}
