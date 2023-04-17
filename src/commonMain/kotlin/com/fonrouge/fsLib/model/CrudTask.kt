package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
enum class CrudTask {
    Create,
    Read,
    Update,
    Delete
}
