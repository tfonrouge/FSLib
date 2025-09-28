package com.fonrouge.base.lib

import com.fonrouge.base.api.CrudTask

/**
 * Returns the appropriate font-awesome icon class name for a specified CRUD operation.
 *
 * @param crudTask The CRUD operation for which the icon is required. It can be one of the values
 *                 from `CrudTask` enum (`Create`, `Read`, `Update`, or `Delete`). If null, no icon is returned.
 * @return The font-awesome class name as a string corresponding to the specified CRUD operation,
 *         or null if the input is null.
 */
fun iconCrud(crudTask: CrudTask? = null): String? {
    return when (crudTask) {
        CrudTask.Create -> "bi bi-file-earmark-plus"
        CrudTask.Read -> "fas fa-eye"
        CrudTask.Update -> "fas fa-edit"
        CrudTask.Delete -> "fas fa-trash-alt"
        null -> null
    }
}
