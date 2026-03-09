package com.fonrouge.ssr.render

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.ssr.model.FieldDef
import kotlinx.html.*

/**
 * DSL context for building form layouts within [PageDef.formBody][com.fonrouge.ssr.PageDef.formBody].
 * Provides layout helpers and the unary plus operator for rendering fields.
 *
 * @param T the model type
 */
class FormContext<T : BaseDoc<*>>(
    /** The kotlinx.html flow content to render into. */
    val flowContent: FlowContent,
    /** The current item being displayed/edited, null for create forms. */
    val item: T?,
    /** The current CRUD operation. */
    val crudTask: CrudTask,
    /** All field definitions for this form. */
    val fields: List<FieldDef<T>>,
    /** Map of field name to error messages. */
    val errors: Map<String, List<String>>,
    /** Raw form values for re-rendering after validation failure. */
    val rawValues: Map<String, String>,
) {
    /**
     * Renders a field using the default form renderer.
     * Usage: `+myFieldDef` inside a [FormContext] block.
     */
    operator fun FieldDef<T>.unaryPlus() {
        flowContent.renderField(this, item, crudTask, errors[this.name], rawValues)
    }

    /** Bootstrap row layout helper. */
    fun row(content: FormContext<T>.() -> Unit) {
        flowContent.div(classes = "row mb-3") {
            derive(this).content()
        }
    }

    /** Bootstrap column layout helper. */
    fun col(width: Int, content: FormContext<T>.() -> Unit) {
        flowContent.div(classes = "col-md-$width") {
            derive(this).content()
        }
    }

    /** Bootstrap card layout helper. */
    fun card(title: String? = null, content: FormContext<T>.() -> Unit) {
        flowContent.div(classes = "card mb-3") {
            div(classes = "card-body") {
                title?.let { h5(classes = "card-title") { +it } }
                derive(this).content()
            }
        }
    }

    /**
     * Creates a derived FormContext targeting a different FlowContent
     * while preserving all other context.
     */
    private fun derive(target: FlowContent): FormContext<T> =
        FormContext(target, item, crudTask, fields, errors, rawValues)
}
