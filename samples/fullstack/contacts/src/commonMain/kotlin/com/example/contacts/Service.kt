package com.example.contacts

import dev.kilua.rpc.annotations.RpcBindingRoute
import dev.kilua.rpc.annotations.RpcService
import kotlinx.serialization.Serializable

/**
 * A summary of the contacts list suitable for frontend display.
 */
@Serializable
data class ContactListResult(
    val data: List<Contact> = emptyList(),
    val total: Int = 0,
)

/**
 * RPC service interface for Contact operations.
 * Shared between JVM (implementation) and JS (client proxy).
 */
@RpcService
interface IContactService {

    /**
     * Returns all contacts.
     */
    @RpcBindingRoute("IContactService.listContacts")
    suspend fun listContacts(): ContactListResult

    /**
     * Creates a new contact and returns it with a generated ID.
     */
    @RpcBindingRoute("IContactService.createContact")
    suspend fun createContact(contact: Contact): Contact

    /**
     * Deletes a contact by ID and returns the updated list.
     */
    @RpcBindingRoute("IContactService.deleteContact")
    suspend fun deleteContact(id: String): ContactListResult
}
