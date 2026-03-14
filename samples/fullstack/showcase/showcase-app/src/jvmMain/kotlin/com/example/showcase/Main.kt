package com.example.showcase

import com.fonrouge.fullStack.memoryDb.InMemoryRepository
import com.fonrouge.fullStack.services.HelpDocsService
import com.fonrouge.fullStack.services.RouteContract
import com.fonrouge.fullStack.services.apiContractEndpoint
import dev.kilua.rpc.applyRoutes
import dev.kilua.rpc.getAllServiceManagers
import dev.kilua.rpc.initRpc
import dev.kilua.rpc.registerService
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.kvision.remote.registerRemoteTypes

/**
 * Ktor application module for the showcase sample.
 * Sets up the [InMemoryRepository] with seed data and registers the RPC service.
 *
 * Serves both the KVision web frontend and Android clients:
 * - Standard Kilua RPC routes for KVision (JSON-RPC 2.0)
 * - `/apiContract` endpoint for Android route discovery
 */
fun Application.main() {
    registerRemoteTypes()
    install(Compression)
    install(WebSockets)

    HelpDocsService.setHelpDocsDir("help-docs")

    val repo = InMemoryRepository<_, Task, String, TaskFilter, String>(
        commonContainer = CommonTask,
    ).seed(seedTasks())

    // Install Kilua RPC routes first — this populates the route registries
    routing {
        getAllServiceManagers().forEach { applyRoutes(it) }
    }
    initRpc {
        registerService<ITaskService> { TaskService(repo) }
        registerService<IShowcaseHelpDocsService> { ShowcaseHelpDocsService(it) }
    }

    // Build API contract by reading actual routes from the registries
    val contract = RouteContract(version = "1.0.0")
    contract.register(TaskServiceManager, "ITaskService")
    contract.register(ShowcaseHelpDocsServiceManager, "IShowcaseHelpDocsService")

    routing {
        apiContractEndpoint(contract)
    }

    // Verify contract consistency
    contract.validate(getAllServiceManagers())
}

/**
 * Generates sample tasks for seeding the in-memory store.
 */
private fun seedTasks(): List<Task> = listOf(
    Task(_id = "1", title = "Setup CI/CD pipeline", description = "Configure GitHub Actions for automated builds and deployments", assignee = "Alice", priority = Priority.HIGH, status = TaskStatus.IN_PROGRESS, estimatedHours = 8),
    Task(_id = "2", title = "Design landing page", description = "Create wireframes and mockups for the new product landing page", assignee = "Bob", priority = Priority.MEDIUM, status = TaskStatus.OPEN, estimatedHours = 12),
    Task(_id = "3", title = "Write unit tests", description = "Add test coverage for the authentication module", assignee = "Carol", priority = Priority.HIGH, status = TaskStatus.OPEN, estimatedHours = 6),
    Task(_id = "4", title = "Database migration", description = "Migrate user data from legacy system to new schema", assignee = "Dave", priority = Priority.CRITICAL, status = TaskStatus.IN_PROGRESS, estimatedHours = 16),
    Task(_id = "5", title = "Update documentation", description = "Review and update the API reference docs for v2.0", assignee = "Eve", priority = Priority.LOW, status = TaskStatus.DONE, estimatedHours = 4),
    Task(_id = "6", title = "Fix login bug", description = "Users report intermittent 401 errors on mobile browsers", assignee = "Alice", priority = Priority.CRITICAL, status = TaskStatus.OPEN, estimatedHours = 3),
    Task(_id = "7", title = "Optimize queries", description = "Review and optimize slow database queries in reports module", assignee = "Dave", priority = Priority.MEDIUM, status = TaskStatus.OPEN, estimatedHours = 10),
    Task(_id = "8", title = "Code review backlog", description = "Clear pending pull requests in the review queue", assignee = "Carol", priority = Priority.MEDIUM, status = TaskStatus.IN_PROGRESS, estimatedHours = 5),
    Task(_id = "9", title = "Add dark mode", description = "Implement dark mode toggle with CSS custom properties", assignee = "Bob", priority = Priority.LOW, status = TaskStatus.OPEN, estimatedHours = 8),
    Task(_id = "10", title = "Security audit", description = "Run OWASP ZAP scan and address findings", assignee = "Eve", priority = Priority.HIGH, status = TaskStatus.OPEN, estimatedHours = 12),
    Task(_id = "11", title = "Performance profiling", description = "Profile frontend bundle and reduce initial load time", assignee = "Alice", priority = Priority.MEDIUM, status = TaskStatus.DONE, estimatedHours = 6),
    Task(_id = "12", title = "Refactor notification service", description = "Extract notification logic into a standalone microservice", assignee = "Dave", priority = Priority.LOW, status = TaskStatus.CANCELLED, estimatedHours = 20),
)
