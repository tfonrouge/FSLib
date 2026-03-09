package com.example.ssrsample.basic

import com.fonrouge.ssr.plugin.FsSsr
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Minimal SSR sample — a single Todo entity with default layout.
 * Demonstrates the least amount of code needed to get a working CRUD app.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        val repo = InMemoryRepository(CommonTodo)
        repo.store["1"] = Todo(_id = "1", title = "Learn Kotlin")
        repo.store["2"] = Todo(_id = "2", title = "Build SSR app", done = true)
        repo.store["3"] = Todo(_id = "3", title = "Deploy to production")

        install(FsSsr) {
            page(TodoPage(repo))
        }

        routing {
            get("/") { call.respondRedirect("/todos") }
        }
    }.start(wait = true)
}
