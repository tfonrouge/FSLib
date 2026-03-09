package com.fonrouge.ssr.routing

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.ssr.PageDef
import com.fonrouge.ssr.auth.AllowAllAuth
import com.fonrouge.ssr.auth.SsrAuth
import com.fonrouge.ssr.bind.FormBinder
import com.fonrouge.ssr.context.RequestContext
import com.fonrouge.ssr.layout.SsrLayout
import com.fonrouge.ssr.model.FlashMessage
import com.fonrouge.ssr.model.SsrHookResult
import com.fonrouge.ssr.render.PageRenderer
import com.fonrouge.ssr.session.addFlash
import com.fonrouge.ssr.session.consumeFlash
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Installs CRUD routes for a [PageDef] into the Ktor routing tree.
 *
 * Generated routes:
 * - `GET  {basePath}`             — list page
 * - `GET  {basePath}/new`         — create form
 * - `POST {basePath}`             — create action
 * - `GET  {basePath}/{id}`        — detail (read-only view)
 * - `GET  {basePath}/{id}/edit`   — update form
 * - `POST {basePath}/{id}`        — update action
 * - `POST {basePath}/{id}/delete` — delete action
 *
 * @param CC the common container type
 * @param T the document model type
 * @param ID the document ID type
 * @param FILT the API filter type
 * @param pageDef the page definition
 * @param layout the page layout
 * @param auth the auth strategy, defaults to [AllowAllAuth]
 */
fun <CC, T, ID, FILT> Route.installCrudRoutes(
    pageDef: PageDef<CC, T, ID, FILT>,
    layout: SsrLayout,
    auth: SsrAuth = AllowAllAuth(),
) where CC : ICommonContainer<T, ID, FILT>,
        T : BaseDoc<ID>,
        ID : Any,
        FILT : IApiFilter<*> {

    val renderer = PageRenderer(pageDef, layout)
    val binder = FormBinder(pageDef.commonContainer.itemKClass, pageDef.fields)

    route(pageDef.basePath) {

        // ---- LIST ----
        get {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Read, pageDef)

            val hookResult = pageDef.onBeforeList(ctx)
            if (handleHookResult(call, hookResult)) return@get

            val page = call.parameters["page"]?.toIntOrNull() ?: 1

            val apiList = ApiList(
                tabPage = page,
                tabSize = pageDef.pageSize,
                apiFilter = ctx.apiFilter,
            )
            val listState = pageDef.repository.apiListProcess(call, apiList)

            val flash = call.consumeFlash()
            call.respondHtml {
                with(renderer) { renderListPage(listState, page, flash) }
            }
        }

        // ---- CREATE FORM ----
        get("/new") {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Create, pageDef)

            val hookResult = pageDef.onBeforeForm(null, CrudTask.Create, ctx)
            if (handleHookResult(call, hookResult)) return@get

            val flash = call.consumeFlash()
            call.respondHtml {
                with(renderer) { renderFormPage(null, CrudTask.Create, flashMessages = flash) }
            }
        }

        // ---- CREATE ACTION ----
        post {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Create, pageDef)

            val params = call.receiveParameters()
            val bindResult = binder.bindAndValidate(params)

            if (bindResult.hasErrors) {
                call.respondHtml {
                    with(renderer) {
                        renderFormPage(null, CrudTask.Create, bindResult.errors, bindResult.rawValues)
                    }
                }
                return@post
            }

            val itemState = pageDef.repository.insertOne(bindResult.value!!, ctx.apiFilter, call)
            if (itemState.hasError) {
                call.respondHtml {
                    with(renderer) {
                        renderFormPage(
                            null, CrudTask.Create,
                            mapOf("_global" to listOf(itemState.msgError ?: "Error creating record")),
                            bindResult.rawValues,
                        )
                    }
                }
                return@post
            }

            call.addFlash(FlashMessage.success("Record created successfully"))
            val redirect = pageDef.onAfterAction(itemState.item!!, CrudTask.Create, ctx)
            call.respondRedirect(redirect)
        }

        // ---- READ (DETAIL) ----
        get("/{id}") {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Read, pageDef)

            val id = pageDef.parseId(call.parameters["id"]!!)
            val item = pageDef.repository.findById(id, ctx.apiFilter)

            if (item == null) {
                call.addFlash(FlashMessage.error("Record not found"))
                call.respondRedirect(pageDef.basePath)
                return@get
            }

            val hookResult = pageDef.onBeforeForm(item, CrudTask.Read, ctx)
            if (handleHookResult(call, hookResult)) return@get

            val flash = call.consumeFlash()
            call.respondHtml {
                with(renderer) { renderFormPage(item, CrudTask.Read, flashMessages = flash) }
            }
        }

        // ---- UPDATE FORM ----
        get("/{id}/edit") {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Update, pageDef)

            val id = pageDef.parseId(call.parameters["id"]!!)
            val item = pageDef.repository.findById(id, ctx.apiFilter)

            if (item == null) {
                call.addFlash(FlashMessage.error("Record not found"))
                call.respondRedirect(pageDef.basePath)
                return@get
            }

            val hookResult = pageDef.onBeforeForm(item, CrudTask.Update, ctx)
            if (handleHookResult(call, hookResult)) return@get

            val flash = call.consumeFlash()
            call.respondHtml {
                with(renderer) { renderFormPage(item, CrudTask.Update, flashMessages = flash) }
            }
        }

        // ---- UPDATE ACTION ----
        post("/{id}") {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Update, pageDef)

            val id = pageDef.parseId(call.parameters["id"]!!)
            val existing = pageDef.repository.findById(id, ctx.apiFilter)

            if (existing == null) {
                call.addFlash(FlashMessage.error("Record not found"))
                call.respondRedirect(pageDef.basePath)
                return@post
            }

            val params = call.receiveParameters()
            val bindResult = binder.bindAndValidate(params, existing)

            if (bindResult.hasErrors) {
                call.respondHtml {
                    with(renderer) {
                        renderFormPage(existing, CrudTask.Update, bindResult.errors, bindResult.rawValues)
                    }
                }
                return@post
            }

            val itemState = pageDef.repository.updateOne(bindResult.value!!, ctx.apiFilter, call)
            if (itemState.hasError) {
                call.respondHtml {
                    with(renderer) {
                        renderFormPage(
                            existing, CrudTask.Update,
                            mapOf("_global" to listOf(itemState.msgError ?: "Error updating record")),
                            bindResult.rawValues,
                        )
                    }
                }
                return@post
            }

            call.addFlash(FlashMessage.success("Record updated successfully"))
            val redirect = pageDef.onAfterAction(itemState.item!!, CrudTask.Update, ctx)
            call.respondRedirect(redirect)
        }

        // ---- DELETE ACTION ----
        post("/{id}/delete") {
            val ctx = buildContext(call, pageDef)
            checkPermissionOrDeny(call, auth, CrudTask.Delete, pageDef)

            val id = pageDef.parseId(call.parameters["id"]!!)
            val itemState = pageDef.repository.deleteOne(id, ctx.apiFilter)

            if (itemState.hasError) {
                call.addFlash(FlashMessage.error(itemState.msgError ?: "Cannot delete record"))
            } else {
                call.addFlash(FlashMessage.success("Record deleted successfully"))
            }
            call.respondRedirect(pageDef.basePath)
        }
    }
}

/**
 * Builds a [RequestContext] with the default API filter from the page definition.
 */
private fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>
    buildContext(call: ApplicationCall, pageDef: PageDef<CC, T, ID, FILT>): RequestContext<FILT> {
    val apiFilter = pageDef.commonContainer.apiFilterInstance()
    return RequestContext.from(call, apiFilter)
}

/**
 * Checks permissions and responds with 403 if denied.
 */
private suspend fun checkPermissionOrDeny(
    call: ApplicationCall,
    auth: SsrAuth,
    crudTask: CrudTask,
    pageDef: PageDef<*, *, *, *>,
) {
    val result = auth.checkPermission(call, crudTask, pageDef.repository)
    if (result.hasError) {
        call.respondText(
            result.msgError ?: "Access denied",
            status = HttpStatusCode.Forbidden,
        )
    }
}

/**
 * Handles a lifecycle hook result. Returns true if the response was handled (redirect).
 */
private suspend fun handleHookResult(call: ApplicationCall, result: SsrHookResult): Boolean {
    return when (result) {
        is SsrHookResult.Continue -> false
        is SsrHookResult.Redirect -> {
            result.flash?.let { call.addFlash(it) }
            call.respondRedirect(result.url)
            true
        }
    }
}
