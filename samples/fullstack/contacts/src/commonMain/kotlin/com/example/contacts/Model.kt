package com.example.contacts

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/**
 * A contact entry.
 */
@Serializable
data class Contact(
    override val _id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val role: String = "",
) : BaseDoc<String>

/**
 * API filter for Contact queries.
 */
@Serializable
class ContactFilter : IApiFilter<String>()

/**
 * Metadata container for [Contact].
 */
object CommonContact : ICommonContainer<Contact, String, ContactFilter>(
    itemKClass = Contact::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = ContactFilter.serializer(),
    labelItem = "Contact",
    labelList = "Contacts",
)
