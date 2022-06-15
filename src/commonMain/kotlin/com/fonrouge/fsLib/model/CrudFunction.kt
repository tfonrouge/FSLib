package com.fonrouge.fsLib.model

@kotlinx.serialization.Serializable
enum class CrudFunction {
    CREATE,
    READ,
    UPDATE,
    DELETE
}
