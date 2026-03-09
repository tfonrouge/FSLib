package com.example.ssrsample.catalog

import com.fonrouge.ssr.plugin.FsSsr
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Entry point for the SSR sample application.
 * Starts a Ktor/Netty server with CRUD pages for Products and Customers.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

/**
 * Configures the Ktor application module with SSR CRUD routes and seed data.
 */
fun Application.module() {
    install(Compression)
    install(CallLogging)

    // Create repositories and seed sample data
    val productRepo = InMemoryRepository(CommonProduct)
    val customerRepo = InMemoryRepository(CommonCustomer)
    seedData(productRepo, customerRepo)

    // Install the SSR plugin with both page definitions
    install(FsSsr) {
        layout = AppLayout()
        page(ProductPage(productRepo))
        page(CustomerPage(customerRepo))
    }

    // Home page redirect
    routing {
        get("/") {
            call.respondRedirect("/products")
        }
    }
}

/**
 * Populates repositories with sample data for demonstration purposes.
 */
private fun seedData(
    productRepo: InMemoryRepository<*, Product, *>,
    customerRepo: InMemoryRepository<*, Customer, *>,
) {
    productRepo.store["p1"] = Product(
        _id = "p1", name = "Wireless Mouse", description = "Ergonomic wireless mouse with USB receiver",
        price = 29.99, category = "Electronics",
    )
    productRepo.store["p2"] = Product(
        _id = "p2", name = "Kotlin in Action", description = "Comprehensive guide to the Kotlin language",
        price = 44.99, category = "Books",
    )
    productRepo.store["p3"] = Product(
        _id = "p3", name = "Standing Desk Mat", description = "Anti-fatigue mat for standing desks",
        price = 39.99, category = "Home",
    )
    productRepo.store["p4"] = Product(
        _id = "p4", name = "Running Shoes", description = "Lightweight trail running shoes",
        price = 89.99, category = "Sports", inStock = false,
    )

    customerRepo.store["c1"] = Customer(
        _id = "c1", firstName = "Alice", lastName = "Smith",
        email = "alice@example.com", phone = "555-0101", city = "New York",
    )
    customerRepo.store["c2"] = Customer(
        _id = "c2", firstName = "Bob", lastName = "Johnson",
        email = "bob@example.com", phone = "555-0102", city = "San Francisco",
    )
    customerRepo.store["c3"] = Customer(
        _id = "c3", firstName = "Carol", lastName = "Williams",
        email = "carol@example.com", phone = "555-0103", city = "Chicago", active = false,
    )
}
