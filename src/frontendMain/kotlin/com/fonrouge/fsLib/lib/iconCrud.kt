package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.CrudAction

fun iconCrud(crudAction: CrudAction? = null): String? {
    return when (crudAction) {
        CrudAction.Create -> "fas fa-plus"
        CrudAction.Read -> "fas fa-eye"
        CrudAction.Update -> "fas fa-edit"
        CrudAction.Delete -> "fas fa-trash-alt"
        null -> null
    }
}
