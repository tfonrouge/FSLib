package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
enum class CrudAction {
    Create,
    Read,
    Update,
    Delete
}
