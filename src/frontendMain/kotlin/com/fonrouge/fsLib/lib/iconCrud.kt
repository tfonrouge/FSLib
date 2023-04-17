package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.CrudTask

fun iconCrud(crudTask: CrudTask? = null): String? {
    return when (crudTask) {
        CrudTask.Create -> "fas fa-plus"
        CrudTask.Read -> "fas fa-eye"
        CrudTask.Update -> "fas fa-edit"
        CrudTask.Delete -> "fas fa-trash-alt"
        null -> null
    }
}
