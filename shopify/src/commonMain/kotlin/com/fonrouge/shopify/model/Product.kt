package com.fonrouge.shopify.model

import com.fonrouge.base.types.StringId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("id")
    override val _id: StringId<Product>,
    override val title: String,
    override val handle: String? = null,
    override val vendor: String? = null
) : IProduct<StringId<Product>>
