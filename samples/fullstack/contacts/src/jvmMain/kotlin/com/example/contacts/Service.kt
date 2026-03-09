package com.example.contacts

/**
 * Server-side implementation of [IContactService].
 * Delegates operations to [ContactStore].
 */
class ContactService(private val store: ContactStore) : IContactService {

    override suspend fun listContacts(): ContactListResult {
        val all = store.data.values.toList()
        return ContactListResult(data = all, total = all.size)
    }

    override suspend fun createContact(contact: Contact): Contact {
        val id = if (contact._id.isBlank()) java.util.UUID.randomUUID().toString() else contact._id
        val newItem = contact.copy(_id = id)
        store.data[id] = newItem
        return newItem
    }

    override suspend fun deleteContact(id: String): ContactListResult {
        store.data.remove(id)
        return listContacts()
    }
}
