package com.fonrouge.ssr.model

/**
 * Definition of a list column for server-side table rendering.
 * Analogous to Tabulator column definitions in the KVision frontend.
 *
 * @param T the model type this column displays
 */
class ColumnDef<T>(
    /** Column identifier, typically the property name. */
    val name: String,
    /** Display label in the table header. */
    val label: String,
    /** Extracts the display value from a model instance. */
    val accessor: (T) -> String,
) {
    /** Whether this column supports sorting. */
    var sortable: Boolean = false

    /** Whether this column supports header filtering. */
    var filterable: Boolean = false

    /** CSS class applied to header cells. */
    var headerClass: String? = null

    /** CSS class applied to data cells. */
    var cellClass: String? = null

    /** Column width (e.g. "200px", "20%"). */
    var width: String? = null

    /** Custom cell renderer returning raw HTML. Null uses [accessor] text. */
    var renderHtml: ((T) -> String)? = null

    /** Marks this column as sortable. */
    fun sortable() {
        sortable = true
    }

    /** Marks this column as filterable. */
    fun filterable() {
        filterable = true
    }

    /**
     * Renders the cell value as a Bootstrap badge.
     * [mapping] maps field values to Bootstrap color names (e.g. "success", "danger").
     */
    fun badge(mapping: Map<String, String>) {
        renderHtml = { item ->
            val value = accessor(item)
            val color = mapping[value] ?: "secondary"
            """<span class="badge bg-$color">$value</span>"""
        }
    }

    /**
     * Applies a named format to the column value.
     * Supported formats: "date" (shows first 10 chars).
     */
    fun format(type: String) {
        renderHtml = { item ->
            val raw = accessor(item)
            when (type) {
                "date" -> raw.take(10)
                else -> raw
            }
        }
    }
}
