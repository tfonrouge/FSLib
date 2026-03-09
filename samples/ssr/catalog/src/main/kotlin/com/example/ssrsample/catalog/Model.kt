package com.example.ssrsample.catalog

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

// ── Models ──────────────────────────────────────────────────

/**
 * A product in the catalog.
 */
@Serializable
data class Product(
    override val _id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val inStock: Boolean = true,
) : BaseDoc<String>

/**
 * A customer record.
 */
@Serializable
data class Customer(
    override val _id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val active: Boolean = true,
) : BaseDoc<String>

// ── Filters ─────────────────────────────────────────────────

/** API filter for Product queries. */
@Serializable
class ProductFilter : IApiFilter<String>()

/** API filter for Customer queries. */
@Serializable
class CustomerFilter : IApiFilter<String>()

// ── Common Containers ───────────────────────────────────────

/** Metadata container for [Product]. */
object CommonProduct : ICommonContainer<Product, String, ProductFilter>(
    itemKClass = Product::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = ProductFilter.serializer(),
    labelItem = "Product",
    labelList = "Products",
)

/** Metadata container for [Customer]. */
object CommonCustomer : ICommonContainer<Customer, String, CustomerFilter>(
    itemKClass = Customer::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = CustomerFilter.serializer(),
    labelItem = "Customer",
    labelList = "Customers",
)
