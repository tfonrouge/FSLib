package com.example.contacts

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable

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
 * Metadata container for [Contact].
 */
object CommonContact : ICommonContainer<Contact, String, ApiFilter>(
    itemKClass = Contact::class,
    filterKClass = ApiFilter::class,
    labelItem = "Contact",
    labelList = "Contacts",
)
