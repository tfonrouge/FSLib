package com.fonrouge.ssr.render

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.ssr.PageDef
import kotlinx.html.*

/**
 * Renders the list page for a [PageDef], including toolbar, data table,
 * row actions, and pagination.
 *
 * @param T the model type
 * @param pageDef the page definition with column configs
 * @param items the data items to display
 * @param currentPage the current page number (1-based)
 * @param lastPage the total number of pages, null if unknown
 */
fun <T : BaseDoc<*>> FlowContent.renderList(
    pageDef: PageDef<T, *, *>,
    items: List<T>,
    currentPage: Int,
    lastPage: Int?,
) {
    // Alpine.js state for row selection
    div {
        attributes["x-data"] = "{ selected: null }"

        // Toolbar
        div(classes = "d-flex justify-content-between align-items-center mb-3") {
            h4(classes = "mb-0") { +pageDef.title }
            div(classes = "d-flex gap-2") {
                a(href = "${pageDef.basePath}/new", classes = "btn btn-primary btn-sm") {
                    +"New ${pageDef.titleItem}"
                }
                pageDef.apply { listToolbarExtra() }
            }
        }

        // Table
        if (items.isEmpty()) {
            div(classes = "text-center text-muted py-5") {
                p { +"No records found." }
            }
        } else {
            div(classes = "table-responsive") {
                table(classes = "table table-striped table-hover align-middle") {
                    thead(classes = "table-light") {
                        tr {
                            pageDef.columns.forEach { col ->
                                th {
                                    col.width?.let { style = "width: $it" }
                                    col.headerClass?.let { classes = setOf(it) }
                                    +col.label
                                }
                            }
                            th(classes = "text-end") { +"Actions" }
                        }
                    }
                    tbody {
                        items.forEach { item ->
                            val itemId = getIdString(item)
                            tr {
                                attributes["x-on:click"] = "selected = '$itemId'"
                                attributes[":class"] = "selected === '$itemId' ? 'table-active' : ''"
                                attributes["style"] = "cursor: pointer"

                                pageDef.columns.forEach { col ->
                                    td {
                                        col.cellClass?.let { classes = setOf(it) }
                                        val html = col.renderHtml
                                        if (html != null) {
                                            unsafe { +html(item) }
                                        } else {
                                            +col.accessor(item)
                                        }
                                    }
                                }

                                // Row actions
                                td(classes = "text-end") {
                                    div(classes = "btn-group btn-group-sm") {
                                        a(
                                            href = "${pageDef.basePath}/$itemId",
                                            classes = "btn btn-outline-secondary",
                                        ) { +"View" }
                                        a(
                                            href = "${pageDef.basePath}/$itemId/edit",
                                            classes = "btn btn-outline-primary",
                                        ) { +"Edit" }
                                        // Delete via form POST with confirmation
                                        form(
                                            action = "${pageDef.basePath}/$itemId/delete",
                                            method = FormMethod.post,
                                            classes = "d-inline",
                                        ) {
                                            button(
                                                type = ButtonType.submit,
                                                classes = "btn btn-outline-danger btn-sm",
                                            ) {
                                                attributes["onclick"] =
                                                    "return confirm('Are you sure you want to delete this record?')"
                                                +"Delete"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pagination
        if (lastPage != null && lastPage > 1) {
            nav {
                ul(classes = "pagination justify-content-center") {
                    // Previous
                    li(classes = "page-item${if (currentPage <= 1) " disabled" else ""}") {
                        a(
                            href = "${pageDef.basePath}?page=${currentPage - 1}",
                            classes = "page-link",
                        ) { +"Previous" }
                    }
                    // Page numbers
                    for (p in 1..lastPage) {
                        li(classes = "page-item${if (p == currentPage) " active" else ""}") {
                            a(
                                href = "${pageDef.basePath}?page=$p",
                                classes = "page-link",
                            ) { +"$p" }
                        }
                    }
                    // Next
                    li(classes = "page-item${if (currentPage >= lastPage) " disabled" else ""}") {
                        a(
                            href = "${pageDef.basePath}?page=${currentPage + 1}",
                            classes = "page-link",
                        ) { +"Next" }
                    }
                }
            }
        }
    }
}

/**
 * Extracts the string representation of the _id from a [BaseDoc] instance.
 */
private fun <T : BaseDoc<*>> getIdString(item: T): String = item._id.toString()
