package com.example.contacts

/**
 * Simple in-memory store for contacts.
 */
class ContactStore {
    /** In-memory data store keyed by contact ID. */
    val data = mutableMapOf<String, Contact>()
}
