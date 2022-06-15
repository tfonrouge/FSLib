package com.fonrouge.fsLib.model

@kotlinx.serialization.Serializable
enum class CrudAction {
    Create,
    Read,
    Update,
    Delete
}
