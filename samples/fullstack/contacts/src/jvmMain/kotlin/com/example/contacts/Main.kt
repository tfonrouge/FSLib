package com.example.contacts

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
 * Ktor application module for the contacts sample.
 * Registers the [IContactService] with an in-memory store seeded with sample data.
 */
fun Application.main() {
    registerRemoteTypes()
    install(Compression)
    install(WebSockets)

    val store = ContactStore()
    seedData(store)

    routing {
        getAllServiceManagers().forEach { applyRoutes(it) }
    }
    initRpc {
        registerService<IContactService> { ContactService(store) }
    }
}

/**
 * Populates the store with sample contacts.
 */
private fun seedData(store: ContactStore) {
    listOf(
        Contact(_id = "1", firstName = "Alice", lastName = "Smith", email = "alice@example.com", phone = "555-0101", company = "Acme Corp", role = "Engineer"),
        Contact(_id = "2", firstName = "Bob", lastName = "Johnson", email = "bob@example.com", phone = "555-0102", company = "Acme Corp", role = "Manager"),
        Contact(_id = "3", firstName = "Carol", lastName = "Williams", email = "carol@example.com", phone = "555-0103", company = "Globex Inc", role = "Designer"),
        Contact(_id = "4", firstName = "Dave", lastName = "Brown", email = "dave@example.com", phone = "555-0104", company = "Globex Inc", role = "Developer"),
        Contact(_id = "5", firstName = "Eve", lastName = "Davis", email = "eve@example.com", phone = "555-0105", company = "Initech", role = "Analyst"),
    ).forEach { store.data[it._id] = it }
}
