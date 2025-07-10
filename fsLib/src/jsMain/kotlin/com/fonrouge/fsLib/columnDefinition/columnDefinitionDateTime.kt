package com.fonrouge.fsLib.columnDefinition

import com.fonrouge.fsLib.fieldName
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.js.Tabulator
import io.kvision.types.OffsetDateTime
import kotlin.js.json
import kotlin.reflect.KProperty1

/**
 * Creates a column configuration for a Tabulator table, specifically for properties of type `OffsetDateTime?`.
 *
 * @param kProperty1 The Kotlin property (`KProperty1`) representing the field in the data model to be used
 *                   within the column definition.
 * @param title The title of the column. Defaults to the name of the property if not specified.
 * @param cellEdited An optional lambda function to handle the event when a cell within this column has been edited.
 * @return A `ColumnDefinition` object configured with the provided property, title, editor, and formatter.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> columnDefinitionDateTime(
    kProperty1: KProperty1<T, OffsetDateTime?>,
    title: String = kProperty1.name,
    cellEdited: ((Tabulator.CellComponent) -> Unit)? = null,
): ColumnDefinition<T> = ColumnDefinition(
    title = "<i class=\"fa-solid fa-calendar\"></i> $title",
    headerHozAlign = Align.CENTER,
    field = fieldName(kProperty1),
    formatter = Formatter.DATETIME,
    formatterParams = json(
        "inputFormat" to "iso",
        "outputFormat" to "EEE dd MMM y HH:mm",
        "invalidPlaceholder" to "(invalid date)",
    ),
    editor = cellEdited?.let { Editor.DATETIME },
    editorParams = json(
        "format" to "iso"
    ),
    cellEdited = cellEdited
)
