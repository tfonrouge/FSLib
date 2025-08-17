package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.api.CrudTask

/**
 * Maps a CRUD task to its corresponding Font Awesome icon class.
 *
 * @param crudTask The CRUD operation for which the icon is needed. Defaults to null.
 *                 Possible values are:
 *                 - `CrudTask.Create`: Maps to "fas fa-plus".
 *                 - `CrudTask.Read`: Maps to "fas fa-eye".
 *                 - `CrudTask.Update`: Maps to "fas fa-edit".
 *                 - `CrudTask.Delete`: Maps to "fas fa-trash-alt".
 * @return A string representing the Font Awesome icon class for the given CRUD task, or null if no task is provided.
 */
fun iconCrud(crudTask: CrudTask? = null): String? {
    return when (crudTask) {
        CrudTask.Create -> "fas fa-plus"
        CrudTask.Read -> "fas fa-eye"
        CrudTask.Update -> "fas fa-edit"
        CrudTask.Delete -> "fas fa-trash-alt"
        null -> null
    }
}
