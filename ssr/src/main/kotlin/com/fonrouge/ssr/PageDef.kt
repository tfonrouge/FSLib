package com.fonrouge.ssr

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.repository.IRepository
import com.fonrouge.ssr.context.RequestContext
import com.fonrouge.ssr.model.ColumnDef
import com.fonrouge.ssr.model.FieldDef
import com.fonrouge.ssr.model.SsrHookResult
import com.fonrouge.ssr.render.FormContext
import kotlinx.html.FlowContent
import kotlin.reflect.KProperty1

/**
 * Declarative definition of a CRUD page set (list + item views).
 * One PageDef generates 7 routes. Analogous to ConfigView/ConfigViewItem/ConfigViewList
 * in the KVision frontend.
 *
 * Subclasses define columns, fields, and optionally override form layout and lifecycle hooks.
 *
 * @param CC the common container type providing entity metadata
 * @param T the document/model type extending [BaseDoc]
 * @param ID the document identifier type
 * @param FILT the API filter type
 */
abstract class PageDef<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    /** Entity metadata provider (labels, serializers, KClass). */
    val commonContainer: CC,
    /** Repository for CRUD operations. */
    val repository: IRepository<CC, T, ID, FILT, *>,
    /** Display title for the list page. */
    val title: String = commonContainer.labelList,
    /** Display title for single-item pages. */
    val titleItem: String = commonContainer.labelItem,
    /** URL base path for all routes (e.g. "/customers"). */
    val basePath: String = "/${commonContainer.name.lowercase()}",
    /** Number of items per page in the list view. */
    val pageSize: Int = 25,
) {
    /** Column definitions for the list view, in display order. */
    val columns: MutableList<ColumnDef<T>> = mutableListOf()

    /** Field definitions for the form view, in display order. */
    val fields: MutableList<FieldDef<T>> = mutableListOf()

    // ---- Column DSL ----

    /**
     * Defines a list column bound to a model property.
     *
     * @param prop the model property to display
     * @param label column header label, defaults to the property name capitalized
     * @param init optional configuration block
     */
    fun <V> column(
        prop: KProperty1<T, V>,
        label: String = prop.name.replaceFirstChar { it.uppercase() },
        init: ColumnDef<T>.() -> Unit = {},
    ): ColumnDef<T> {
        val col = ColumnDef<T>(prop.name, label, accessor = { prop.get(it)?.toString() ?: "" })
        col.init()
        columns.add(col)
        return col
    }

    /**
     * Defines a computed column not bound to a single property.
     *
     * @param name column identifier
     * @param label column header label
     * @param render function to compute the display value
     * @param init optional configuration block
     */
    fun columnComputed(
        name: String,
        label: String,
        render: (T) -> String,
        init: ColumnDef<T>.() -> Unit = {},
    ): ColumnDef<T> {
        val col = ColumnDef(name, label, accessor = render)
        col.init()
        columns.add(col)
        return col
    }

    // ---- Field DSL ----

    /**
     * Defines a form field bound to a model property.
     *
     * @param prop the model property to bind
     * @param label field label, defaults to the property name capitalized
     * @param init optional configuration block
     */
    fun <V> field(
        prop: KProperty1<T, V>,
        label: String = prop.name.replaceFirstChar { it.uppercase() },
        init: FieldDef<T>.() -> Unit = {},
    ): FieldDef<T> {
        val f = FieldDef<T>(prop.name, label, propertyName = prop.name)
        f.init()
        fields.add(f)
        return f
    }

    // ---- Layout overrides ----

    /**
     * Override to customize form layout using the [FormContext] DSL.
     * Default renders all fields sequentially.
     */
    open fun FormContext<T>.formBody() {
        fields.forEach { +it }
    }

    /**
     * Override to add custom actions to the list toolbar.
     */
    open fun FlowContent.listToolbarExtra() {}

    /**
     * Override to render custom content below the form.
     */
    open fun FlowContent.formExtra(item: T?, crudTask: CrudTask) {}

    // ---- Lifecycle hooks ----

    /**
     * Called before rendering the list page. Can modify context or redirect.
     */
    open suspend fun onBeforeList(ctx: RequestContext<FILT>): SsrHookResult = SsrHookResult.Continue

    /**
     * Called before rendering a form. Can redirect or deny access.
     */
    open suspend fun onBeforeForm(
        item: T?,
        crudTask: CrudTask,
        ctx: RequestContext<FILT>,
    ): SsrHookResult = SsrHookResult.Continue

    /**
     * Called after a successful create/update/delete action.
     * Returns the redirect path. Default returns [basePath].
     */
    open suspend fun onAfterAction(
        item: T,
        crudTask: CrudTask,
        ctx: RequestContext<FILT>,
    ): String = basePath

    // ---- ID conversion ----

    /**
     * Parses a raw string URL parameter into a typed ID.
     * Must be implemented for each concrete PageDef.
     */
    abstract fun parseId(raw: String): ID

    /**
     * Serializes an ID to a string for use in URLs.
     * Default uses [toString].
     */
    open fun serializeId(id: ID): String = id.toString()
}
