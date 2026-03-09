package com.fonrouge.ssr.render

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ListState
import com.fonrouge.ssr.PageDef
import com.fonrouge.ssr.layout.SsrLayout
import com.fonrouge.ssr.model.FlashMessage
import kotlinx.html.*

/**
 * Orchestrates full-page rendering by combining [SsrLayout] with page-specific content.
 * Handles both list and form page types.
 *
 * @param T the model type
 * @param pageDef the page definition
 * @param layout the layout to wrap content in
 */
class PageRenderer<T : BaseDoc<*>>(
    private val pageDef: PageDef<*, T, *, *>,
    private val layout: SsrLayout,
) {
    /**
     * Renders the list page with data from a [ListState].
     */
    fun HTML.renderListPage(
        listState: ListState<T>,
        currentPage: Int,
        flashMessages: List<FlashMessage> = emptyList(),
    ) {
        with(layout) {
            page(pageDef.title, flashMessages) {
                renderList(pageDef, listState.data, currentPage, listState.last_page)
            }
        }
    }

    /**
     * Renders a form page for create, read, update, or delete operations.
     *
     * @param item the model instance (null for create)
     * @param crudTask the CRUD operation type
     * @param errors validation errors map
     * @param rawValues raw form values for re-rendering
     * @param flashMessages pending flash messages
     */
    fun HTML.renderFormPage(
        item: T?,
        crudTask: CrudTask,
        errors: Map<String, List<String>> = emptyMap(),
        rawValues: Map<String, String> = emptyMap(),
        flashMessages: List<FlashMessage> = emptyList(),
    ) {
        val pageTitle = when (crudTask) {
            CrudTask.Create -> "New ${pageDef.titleItem}"
            CrudTask.Read -> pageDef.titleItem
            CrudTask.Update -> "Edit ${pageDef.titleItem}"
            CrudTask.Delete -> "Delete ${pageDef.titleItem}"
        }

        with(layout) {
            page(pageTitle, flashMessages) {
                // Page header with back link
                div(classes = "d-flex justify-content-between align-items-center mb-3") {
                    h4(classes = "mb-0") { +pageTitle }
                    a(href = pageDef.basePath, classes = "btn btn-outline-secondary btn-sm") {
                        +"Back to list"
                    }
                }

                // Global errors
                errors["_global"]?.let { globalErrors ->
                    div(classes = "alert alert-danger") {
                        globalErrors.forEach { p { +it } }
                    }
                }

                // Form
                val formAction = when (crudTask) {
                    CrudTask.Create -> pageDef.basePath
                    CrudTask.Update -> "${pageDef.basePath}/${item?._id}"
                    else -> null
                }

                if (formAction != null) {
                    form(action = formAction, method = FormMethod.post) {
                        val formContext = FormContext(
                            flowContent = this,
                            item = item,
                            crudTask = crudTask,
                            fields = pageDef.fields,
                            errors = errors,
                            rawValues = rawValues,
                        )
                        with(pageDef) { formContext.formBody() }

                        // Form extra content
                        with(pageDef) { formExtra(item, crudTask) }

                        // Submit buttons
                        div(classes = "mt-3 d-flex gap-2") {
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +when (crudTask) {
                                    CrudTask.Create -> "Create"
                                    CrudTask.Update -> "Save changes"
                                    else -> "Submit"
                                }
                            }
                            a(href = pageDef.basePath, classes = "btn btn-outline-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                } else {
                    // Read-only view (no form tag)
                    val formContext = FormContext(
                        flowContent = this,
                        item = item,
                        crudTask = crudTask,
                        fields = pageDef.fields,
                        errors = emptyMap(),
                        rawValues = emptyMap(),
                    )
                    with(pageDef) { formContext.formBody() }

                    // Form extra content
                    with(pageDef) { formExtra(item, crudTask) }

                    // Action buttons for read view
                    item?.let {
                        div(classes = "mt-3 d-flex gap-2") {
                            a(
                                href = "${pageDef.basePath}/${it._id}/edit",
                                classes = "btn btn-primary",
                            ) { +"Edit" }
                            a(href = pageDef.basePath, classes = "btn btn-outline-secondary") {
                                +"Back to list"
                            }
                        }
                    }
                }
            }
        }
    }
}
