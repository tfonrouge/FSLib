package com.example.ssrsample.advanced

import com.fonrouge.ssr.plugin.FsSsr
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Advanced SSR sample — a project/task tracker demonstrating lifecycle hooks,
 * custom validation, card-based form layout, and custom layout with footer.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

/**
 * Configures the Ktor application with SSR CRUD routes and seed data.
 */
fun Application.module() {
    install(Compression)
    install(CallLogging)

    val projectRepo = InMemoryRepository(CommonProject)
    val taskRepo = InMemoryRepository(CommonTask)
    seedData(projectRepo, taskRepo)

    install(FsSsr) {
        layout = AppLayout()
        page(ProjectPage(projectRepo))
        page(TaskPage(projectRepo, taskRepo))
    }

    routing {
        get("/") { call.respondRedirect("/projects") }
    }
}

/**
 * Populates repositories with sample project and task data.
 */
private fun seedData(
    projectRepo: InMemoryRepository<Project, *>,
    taskRepo: InMemoryRepository<Task, *>,
) {
    projectRepo.store["p1"] = Project(
        _id = "p1", code = "WEB", name = "Website Redesign",
        description = "Complete overhaul of the company website", status = "active", owner = "Alice",
    )
    projectRepo.store["p2"] = Project(
        _id = "p2", code = "API", name = "API v2 Migration",
        description = "Migrate REST API from v1 to v2", status = "active", owner = "Bob",
    )
    projectRepo.store["p3"] = Project(
        _id = "p3", code = "LEGACY", name = "Legacy System",
        description = "Maintenance of the old system", status = "archived", owner = "Carol",
    )

    taskRepo.store["t1"] = Task(
        _id = "t1", title = "Design new homepage", projectCode = "WEB",
        priority = "high", status = "in-progress", assignee = "Dave", estimate = 16.0,
        description = "Create wireframes and mockups for the new homepage",
    )
    taskRepo.store["t2"] = Task(
        _id = "t2", title = "Set up CI/CD pipeline", projectCode = "WEB",
        priority = "medium", status = "open", assignee = "Eve", estimate = 8.0,
    )
    taskRepo.store["t3"] = Task(
        _id = "t3", title = "Write endpoint specs", projectCode = "API",
        priority = "high", status = "done", assignee = "Bob", estimate = 12.0,
    )
    taskRepo.store["t4"] = Task(
        _id = "t4", title = "Implement auth middleware", projectCode = "API",
        priority = "high", status = "blocked", assignee = "Alice", estimate = 20.0,
        description = "Blocked: waiting for SSO provider configuration",
    )
    taskRepo.store["t5"] = Task(
        _id = "t5", title = "Database migration script", projectCode = "API",
        priority = "low", status = "open", assignee = "", estimate = 4.0,
    )
}
