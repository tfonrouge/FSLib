package com.fonrouge.fsLib.columnDefinition

import com.fonrouge.fsLib.fieldName
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewList
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.js.Tabulator
import io.kvision.types.OffsetDateTime
import kotlin.js.json
import kotlin.reflect.KProperty1

@Suppress("unused")
fun <T : BaseDoc<*>> ViewList<*, T, *, *, *>.columnDefinitionDateTime(
    kProperty1: KProperty1<T, OffsetDateTime>,
    title: String = kProperty1.name,
    cellEdited: ((Tabulator.CellComponent) -> Unit)? = null
): ColumnDefinition<T> {
    return ColumnDefinition(
        title = title,
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
}
