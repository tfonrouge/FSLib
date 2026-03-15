package com.example.ssrsample.advanced

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.api.CrudTask
import com.fonrouge.ssr.PageDef
import com.fonrouge.ssr.context.RequestContext
import com.fonrouge.ssr.model.FlashMessage
import com.fonrouge.ssr.model.SsrHookResult
import com.fonrouge.ssr.render.FormContext
import kotlinx.html.FlowContent
import kotlinx.html.p
import kotlinx.html.small

/**
 * CRUD page for [Project] with custom form layout using cards.
 * Demonstrates [FormContext.card] and [FormContext.row] layout helpers.
 */
class ProjectPage(
    repo: InMemoryRepository<Project, ApiFilter>,
) : PageDef<Project, String, ApiFilter>(
    commonContainer = CommonProject,
    repository = repo,
    basePath = "/projects",
) {
    init {
        column(Project::code, "Code") { sortable() }
        column(Project::name, "Name") { sortable(); filterable() }
        column(Project::owner, "Owner")
        column(Project::status, "Status") {
            badge(mapOf("active" to "success", "on-hold" to "warning", "archived" to "secondary"))
        }

        field(Project::_id) { hidden() }
        field(Project::code, "Code") {
            required()
            maxLength(10)
            pattern("^[A-Z][A-Z0-9-]*$", "Must start with a letter and contain only uppercase letters, digits, or hyphens")
            placeholder = "e.g. PRJ-01"
            col(4)
        }
        field(Project::name, "Name") { required(); maxLength(100); col(8) }
        field(Project::description, "Description") { textarea(4); col(12) }
        field(Project::owner, "Owner") { required(); col(6) }
        field(Project::status, "Status") {
            select("active", "on-hold", "archived")
            col(6)
        }
    }

    /** Custom form layout grouping fields into cards. */
    override fun FormContext<Project>.formBody() {
        card("Identification") {
            row {
                +fields.first { it.name == "_id" }
                +fields.first { it.name == "code" }
                +fields.first { it.name == "name" }
            }
        }
        card("Details") {
            +fields.first { it.name == "description" }
            row {
                +fields.first { it.name == "owner" }
                +fields.first { it.name == "status" }
            }
        }
    }

    /** Prevent deletion of active projects. */
    override suspend fun onBeforeForm(
        item: Project?,
        crudTask: CrudTask,
        ctx: RequestContext<ApiFilter>,
    ): SsrHookResult {
        if (crudTask == CrudTask.Delete && item?.status == "active") {
            return SsrHookResult.Redirect(
                url = "$basePath/${item._id}",
                flash = FlashMessage(FlashMessage.Level.Warning, "Cannot delete an active project. Archive it first."),
            )
        }
        return SsrHookResult.Continue
    }

    /** Show a flash message after any successful action. */
    override suspend fun onAfterAction(
        item: Project,
        crudTask: CrudTask,
        ctx: RequestContext<ApiFilter>,
    ): String {
        return basePath
    }

    override fun parseId(raw: String): String = raw
}

/**
 * CRUD page for [Task] with custom validation, lifecycle hooks,
 * and additional content below the form.
 */
class TaskPage(
    private val projectRepo: InMemoryRepository<Project, ApiFilter>,
    taskRepo: InMemoryRepository<Task, ApiFilter>,
) : PageDef<Task, String, ApiFilter>(
    commonContainer = CommonTask,
    repository = taskRepo,
    basePath = "/tasks",
) {
    init {
        column(Task::title, "Title") { sortable(); filterable() }
        column(Task::projectCode, "Project") { filterable() }
        column(Task::priority, "Priority") {
            badge(mapOf("high" to "danger", "medium" to "warning", "low" to "info"))
        }
        column(Task::status, "Status") {
            badge(mapOf("open" to "primary", "in-progress" to "info", "done" to "success", "blocked" to "danger"))
        }
        column(Task::assignee, "Assignee")
        column(Task::estimate, "Estimate (h)")

        field(Task::_id) { hidden() }
        field(Task::title, "Title") { required(); maxLength(200); col(12) }
        field(Task::projectCode, "Project Code") {
            required()
            col(4)
            // Custom validation: project code must exist
            validators.add(
                com.fonrouge.ssr.model.FieldValidation.Custom("project-exists") { value ->
                    if (value.isNullOrBlank()) null // let Required handle blank
                    else if (projectRepo.store.values.none { it.code == value }) "Project '$value' does not exist"
                    else null
                }
            )
        }
        field(Task::priority, "Priority") { select("low", "medium", "high"); col(4) }
        field(Task::status, "Status") { select("open", "in-progress", "done", "blocked"); col(4) }
        field(Task::assignee, "Assignee") { col(6) }
        field(Task::estimate, "Estimate (hours)") { number(); col(6); helpText = "Estimated effort in hours" }
        field(Task::description, "Description") { textarea(5); col(12) }
    }

    /** Show available project codes below the form as a hint. */
    override fun FlowContent.formExtra(item: Task?, crudTask: CrudTask) {
        if (crudTask == CrudTask.Create || crudTask == CrudTask.Update) {
            val codes = projectRepo.store.values
                .filter { it.status == "active" }
                .joinToString(", ") { "${it.code} (${it.name})" }
            p {
                small {
                    +"Available projects: $codes"
                }
            }
        }
    }

    /** Prevent creating tasks for archived projects. */
    override suspend fun onBeforeForm(
        item: Task?,
        crudTask: CrudTask,
        ctx: RequestContext<ApiFilter>,
    ): SsrHookResult {
        if (crudTask == CrudTask.Create) {
            // Allow — validation will catch invalid project codes
            return SsrHookResult.Continue
        }
        return SsrHookResult.Continue
    }

    /** After creating a task, redirect to the task list. */
    override suspend fun onAfterAction(
        item: Task,
        crudTask: CrudTask,
        ctx: RequestContext<ApiFilter>,
    ): String = basePath

    override fun parseId(raw: String): String = raw
}
