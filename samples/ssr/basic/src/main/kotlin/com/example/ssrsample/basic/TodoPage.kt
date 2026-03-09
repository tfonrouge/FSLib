package com.example.ssrsample.basic

import com.fonrouge.ssr.PageDef

/**
 * Minimal CRUD page for [Todo] items.
 * Uses default layout, no customization — shows the simplest possible SSR setup.
 */
class TodoPage(
    repo: InMemoryRepository<CommonTodo, Todo, TodoFilter>,
) : PageDef<CommonTodo, Todo, String, TodoFilter>(
    commonContainer = CommonTodo,
    repository = repo,
    basePath = "/todos",
) {
    init {
        column(Todo::title, "Title") { sortable() }
        column(Todo::done, "Done") { badge(mapOf("true" to "success", "false" to "secondary")) }

        field(Todo::_id) { hidden() }
        field(Todo::title, "Title") { required(); col(8) }
        field(Todo::done, "Done") { checkbox() }
    }

    override fun parseId(raw: String): String = raw
}
